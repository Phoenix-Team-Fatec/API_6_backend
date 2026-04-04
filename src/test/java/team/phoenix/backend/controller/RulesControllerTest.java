package team.phoenix.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;
import team.phoenix.backend.service.RulesService;

@WebMvcTest(RulesController.class)
class RulesControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean RulesService rulesService;

    @Test void listRates_noFilter_returnsOk() throws Exception {
        var rate = CommissionRate.builder()
            .id("123")
            .codMarca(10).descrMarca("PRETO")
            .codCargo(100).descriCargo("VENDEDOR LOJA")
            .pctComiss(0.025)
            .versao(1)
            .isVigente(true)
            .createdAt(LocalDateTime.now())
            .build();
        when(rulesService.listRates(null, null)).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/rules/commission-rates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codMarca").value(10))
            .andExpect(jsonPath("$[0].pctComiss").value(0.025))
            .andExpect(jsonPath("$[0].isVigente").value(true));
    }

    @Test void listRates_withCodMarcaFilter_returnsOk() throws Exception {
        var rate = CommissionRate.builder()
            .id("123")
            .codMarca(10).codCargo(100).pctComiss(0.025)
            .versao(1)
            .isVigente(true)
            .createdAt(LocalDateTime.now())
            .build();
        when(rulesService.listRates(10, null)).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/rules/commission-rates").param("codMarca", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codMarca").value(10));
    }

    @Test void createRate_withValidData_returnsCreated() throws Exception {
        var rate = CommissionRate.builder()
            .id("123")
            .codMarca(10).descrMarca("PRETO")
            .codCargo(100).descriCargo("VENDEDOR LOJA")
            .pctComiss(0.025)
            .data(LocalDate.of(2026, 3, 1))
            .textoOriginal("Vendedores da marca PRETO recebem 2.50% de comissão a partir de março de 2026")
            .explicacao("SE (cargo = 'VENDEDOR LOJA' AND marca = 'PRETO')...")
            .versao(1)
            .isVigente(true)
            .createdAt(LocalDateTime.now())
            .build();
        when(rulesService.createRate(any())).thenReturn(rate);

        mockMvc.perform(post("/api/rules")
                .contentType("application/json")
                .content("{\"codMarca\":10,\"descrMarca\":\"PRETO\",\"codCargo\":100,\"descriCargo\":\"VENDEDOR LOJA\",\"pctComiss\":0.025,\"data\":\"2026-03\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codMarca").value(10))
            .andExpect(jsonPath("$.versao").value(1));
    }

    @Test void updateRate_withValidData_returnsOk() throws Exception {
        var updated = CommissionRate.builder()
            .id("123")
            .codMarca(10).descrMarca("PRETO")
            .codCargo(100).descriCargo("VENDEDOR LOJA")
            .pctComiss(0.03)
            .versao(2)
            .isVigente(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        when(rulesService.updateRate(anyString(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/rules/123")
                .contentType("application/json")
                .content("{\"codMarca\":10,\"descrMarca\":\"PRETO\",\"codCargo\":100,\"descriCargo\":\"VENDEDOR LOJA\",\"pctComiss\":0.03}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.versao").value(2));
    }

    @Test void deleteRate_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/rules/123"))
            .andExpect(status().isNoContent());
        verify(rulesService).deactivateRate("123");
    }

    @Test void getRate_found_returnsOk() throws Exception {
        var rate = CommissionRate.builder()
            .id("123")
            .codMarca(10).descrMarca("PRETO")
            .codCargo(100).versao(1).isVigente(true)
            .createdAt(LocalDateTime.now())
            .build();
        when(rulesService.getRateById("123")).thenReturn(Optional.of(rate));

        mockMvc.perform(get("/api/rules/123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("123"));
    }

    @Test void getRate_notFound_returns404() throws Exception {
        when(rulesService.getRateById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rules/999"))
            .andExpect(status().isNotFound());
    }

    @Test void listExceptions_byMonth_returnsOk() throws Exception {
        var ex = MonthlyException.builder().yearMonth(LocalDate.of(2025,7,1))
            .type(ExceptionType.ABSENCE).matricula("MATRIC-58").build();
        when(rulesService.listExceptions(LocalDate.of(2025,7,1), null, null)).thenReturn(List.of(ex));

        mockMvc.perform(get("/api/rules/exceptions").param("month", "2025-07"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].yearMonth").value("2025-07-01"))
            .andExpect(jsonPath("$[0].type").value("ABSENCE"));
    }

    @Test void listExceptions_invalidMonthFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/rules/exceptions").param("month", "07-2025"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid month format. Use yyyy-MM"));
    }
}
