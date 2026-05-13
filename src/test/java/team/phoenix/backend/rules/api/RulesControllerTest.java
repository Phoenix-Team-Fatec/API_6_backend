package team.phoenix.backend.rules.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
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
import team.phoenix.backend.rules.application.GeneratedRuleResult;
import team.phoenix.backend.rules.application.RulesService;

@WebMvcTest(RulesController.class)
class RulesControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean RulesService rulesService;

    @Test void listRates_noFilter_returnsOk() throws Exception {
        var activeRate = CommissionRate.builder()
            .id("123")
            .codMarca(10).descrMarca("PRETO")
            .codCargo(100).descriCargo("VENDEDOR LOJA")
            .pctComiss(0.025)
            .versao(1)
            .isVigente(true)
            .createdAt(LocalDateTime.now())
            .build();
        var inactiveRate = CommissionRate.builder()
            .id("124")
            .codMarca(20).descrMarca("CINZA")
            .codCargo(150).descriCargo("GERENTE QUIOSQUE")
            .pctComiss(0.0125)
            .versao(1)
            .isVigente(false)
            .createdAt(LocalDateTime.now())
            .build();
        when(rulesService.listRates(null, null, null)).thenReturn(List.of(activeRate, inactiveRate));

        mockMvc.perform(get("/api/rules/commission-rates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codMarca").value(10))
            .andExpect(jsonPath("$[0].pctComiss").value(0.025))
            .andExpect(jsonPath("$[0].isVigente").value(true))
            .andExpect(jsonPath("$[1].codMarca").value(20))
            .andExpect(jsonPath("$[1].isVigente").value(false));
    }

    @Test void listRates_withCodMarcaFilter_returnsOk() throws Exception {
        var rate = CommissionRate.builder()
            .id("123")
            .codMarca(10).codCargo(100).pctComiss(0.025)
            .versao(1)
            .isVigente(true)
            .createdAt(LocalDateTime.now())
            .build();
        when(rulesService.listRates(10, null, null)).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/rules/commission-rates").param("codMarca", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codMarca").value(10));
    }

    @Test void listRates_withIsVigenteTrue_returnsOnlyActive() throws Exception {
        var rate = CommissionRate.builder()
            .id("123")
            .nomeRegra("Comissao PRETO Marco")
            .codMarca(10).descrMarca("PRETO")
            .codCargo(100).descriCargo("VENDEDOR LOJA")
            .pctComiss(0.025)
            .versao(1)
            .isVigente(true)
            .createdAt(LocalDateTime.now())
            .build();
        when(rulesService.listRates(null, null, true)).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/rules/commission-rates").param("isVigente", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].isVigente").value(true));
    }

    @Test void listRates_withIsVigenteFalse_returnsOnlyInactive() throws Exception {
        var rate = CommissionRate.builder()
            .id("123")
            .nomeRegra("Comissao PRETO Marco")
            .codMarca(10).descrMarca("PRETO")
            .codCargo(100).descriCargo("VENDEDOR LOJA")
            .pctComiss(0.025)
            .versao(1)
            .isVigente(false)
            .createdAt(LocalDateTime.now())
            .build();
        when(rulesService.listRates(null, null, false)).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/rules/commission-rates").param("isVigente", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].isVigente").value(false));
    }

    @Test void listTrashedRates_noFilter_returnsOk() throws Exception {
        var trashed = CommissionRate.builder()
            .id("999")
            .codMarca(20).descrMarca("CINZA")
            .codCargo(150).descriCargo("GERENTE")
            .pctComiss(0.01)
            .isVigente(false)
            .deletedAt(LocalDateTime.now())
            .build();
        when(rulesService.listTrashedRates(null, null, null)).thenReturn(List.of(trashed));

        mockMvc.perform(get("/api/rules/commission-rates/trash"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("999"));
    }

    @Test void listTrashedRates_withFilter_returnsOk() throws Exception {
        var trashed = CommissionRate.builder()
            .id("999")
            .codMarca(20).descrMarca("CINZA")
            .codCargo(150).descriCargo("GERENTE")
            .pctComiss(0.01)
            .isVigente(false)
            .deletedAt(LocalDateTime.now())
            .build();
        when(rulesService.listTrashedRates(20, null, false)).thenReturn(List.of(trashed));

        mockMvc.perform(get("/api/rules/commission-rates/trash")
                .param("codMarca", "20")
                .param("isVigente", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codMarca").value(20));
    }

    @Test void createRate_withValidData_returnsCreated() throws Exception {
        var rate = CommissionRate.builder()
            .id("123")
            .nomeRegra("Comissao PRETO Marco")
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
                .content("{\"nomeRegra\":\"Comissao PRETO Marco\",\"codMarca\":10,\"descrMarca\":\"PRETO\",\"codCargo\":100,\"descriCargo\":\"VENDEDOR LOJA\",\"pctComiss\":0.025,\"data\":\"2026-03\",\"textoOriginal\":\"Texto manual\",\"explicacao\":\"Explicacao manual\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nomeRegra").value("Comissao PRETO Marco"))
            .andExpect(jsonPath("$.codMarca").value(10))
            .andExpect(jsonPath("$.versao").value(1));

        verify(rulesService).createRate(argThat(r ->
            "Comissao PRETO Marco".equals(r.getNomeRegra()) &&
            "Texto manual".equals(r.getTextoOriginal()) &&
            "Explicacao manual".equals(r.getExplicacao())
        ));
    }

    @Test void createRate_whenServiceRejectsRule_returnsBadRequest() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("codMarca is required"))
            .when(rulesService).createRate(any());

        mockMvc.perform(post("/api/rules")
                .contentType("application/json")
                .content("{\"descrMarca\":\"PRETO\",\"codCargo\":100,\"descriCargo\":\"VENDEDOR LOJA\",\"pctComiss\":0.025}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Erro ao criar regra: codMarca is required"));
    }

    @Test void generateRule_fromNaturalLanguage_returnsCreatedObjects() throws Exception {
        var rate = CommissionRate.builder()
            .id("rate-1")
            .codMarca(10)
            .descrMarca("PRETO")
            .codCargo(100)
            .descriCargo("VENDEDOR")
            .pctComiss(0.05)
            .data(LocalDate.of(2026, 5, 1))
            .textoOriginal("Criar regra de 5%")
            .explicacao("Regra gerada pela IA")
            .versao(1)
            .isVigente(true)
            .createdAt(LocalDateTime.now())
            .build();
        when(rulesService.generateFromNaturalLanguage("Criar regra de 5%"))
            .thenReturn(new GeneratedRuleResult("override", "Regra gerada pela IA", List.of(rate), List.of()));

        mockMvc.perform(post("/api/rules/generate")
                .contentType("application/json")
                .content("{\"prompt\":\"Criar regra de 5%\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipo").value("override"))
            .andExpect(jsonPath("$.justificativa").value("Regra gerada pela IA"))
            .andExpect(jsonPath("$.rules[0].id").value("rate-1"))
            .andExpect(jsonPath("$.rules[0].textoOriginal").value("Criar regra de 5%"));
    }

    @Test void updateRate_withValidData_returnsOk() throws Exception {
        var updated = CommissionRate.builder()
            .id("123")
            .nomeRegra("Comissao PRETO Atualizada")
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
                .content("{\"nomeRegra\":\"Comissao PRETO Atualizada\",\"codMarca\":10,\"descrMarca\":\"PRETO\",\"codCargo\":100,\"descriCargo\":\"VENDEDOR LOJA\",\"pctComiss\":0.03}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nomeRegra").value("Comissao PRETO Atualizada"))
            .andExpect(jsonPath("$.versao").value(2));

        verify(rulesService).updateRate(anyString(), argThat(r ->
            "Comissao PRETO Atualizada".equals(r.getNomeRegra())
        ));
    }

    @Test void updateRate_withoutData_keepsNullInRequestToService() throws Exception {
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
            .andExpect(status().isOk());

        verify(rulesService).updateRate(anyString(), argThat(r -> r.getData() == null));
    }

    @Test void updateRate_whenServiceRejectsRule_returnsBadRequest() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("pctComiss must be greater than or equal to zero"))
            .when(rulesService).updateRate(anyString(), any());

        mockMvc.perform(put("/api/rules/123")
                .contentType("application/json")
                .content("{\"pctComiss\":-0.01}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Erro ao atualizar regra: pctComiss must be greater than or equal to zero"));
    }

    @Test void updateRate_whenRuleDoesNotExist_returnsNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Regra não encontrada: 123"))
            .when(rulesService).updateRate(anyString(), any());

        mockMvc.perform(put("/api/rules/123")
                .contentType("application/json")
                .content("{\"pctComiss\":0.03}"))
            .andExpect(status().isNotFound());
    }

    @Test void deleteRate_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/rules/123"))
            .andExpect(status().isNoContent());
        verify(rulesService).softDeleteRate("123");
    }

    @Test void deactivateRate_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/rules/123/deactivate"))
            .andExpect(status().isNoContent());
        verify(rulesService).deactivateRate("123");
    }

    @Test void activateRate_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/rules/123/activate"))
            .andExpect(status().isNoContent());
        verify(rulesService).activateRate("123");
    }

    @Test void activateRate_deletedRule_returnsConflict() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("Regra removida não pode ser reativada"))
            .when(rulesService).activateRate("123");

        mockMvc.perform(post("/api/rules/123/activate"))
            .andExpect(status().isConflict())
            .andExpect(content().string("Regra removida não pode ser reativada"));
    }

    @Test void restoreRate_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/rules/123/restore"))
            .andExpect(status().isNoContent());
        verify(rulesService).restoreRate("123");
    }

    @Test void restoreRate_notDeletedRule_returnsConflict() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("Regra não foi deletada"))
            .when(rulesService).restoreRate("123");

        mockMvc.perform(post("/api/rules/123/restore"))
            .andExpect(status().isConflict())
            .andExpect(content().string("Regra não foi deletada"));
    }

    @Test void rollbackRate_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/rules/123/rollback"))
            .andExpect(status().isNoContent());
        verify(rulesService).rollbackRate("123");
    }

    @Test void getRate_found_returnsOk() throws Exception {
        var rate = CommissionRate.builder()
            .id("123")
            .nomeRegra("Comissao PRETO")
            .codMarca(10).descrMarca("PRETO")
            .codCargo(100).versao(1).isVigente(true)
            .createdAt(LocalDateTime.now())
            .build();
        when(rulesService.getRateById("123")).thenReturn(Optional.of(rate));

        mockMvc.perform(get("/api/rules/123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("123"))
            .andExpect(jsonPath("$.nomeRegra").value("Comissao PRETO"));
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

    @Test void listExceptions_multiStore_returnsAlternateStoreFields() throws Exception {
        var ex = MonthlyException.builder().yearMonth(LocalDate.of(2025,7,1))
            .type(ExceptionType.MULTI_STORE).matricula("MATRIC-293")
            .alternateCodLoja(5).daysWorked(10).build();
        when(rulesService.listExceptions(LocalDate.of(2025,7,1), null, null)).thenReturn(List.of(ex));

        mockMvc.perform(get("/api/rules/exceptions").param("month", "2025-07"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].alternateCodLoja").value(5))
            .andExpect(jsonPath("$[0].daysWorked").value(10));
    }

    @Test void listExceptions_invalidMonthFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/rules/exceptions").param("month", "07-2025"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid month format. Use yyyy-MM"));
    }
}
