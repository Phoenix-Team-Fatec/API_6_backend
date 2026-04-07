package team.phoenix.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.service.BusinessRuleService;
import team.phoenix.backend.service.RulesService;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RulesController.class)
class RulesControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean RulesService rulesService;
    @MockitoBean BusinessRuleService businessRuleService;

    @Test void listRates_noFilter_returnsOk() throws Exception {
        var rate = CommissionRate.builder().codMarca(10).descrMarca("PRETO")
            .codCargo(100).descriCargo("VENDEDOR LOJA").pctComiss(0.025).build();
        when(rulesService.listRates(null, null)).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/rules/commission-rates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codMarca").value(10))
            .andExpect(jsonPath("$[0].pctComiss").value(0.025));
    }

    @Test void listRates_withCodMarcaFilter_returnsOk() throws Exception {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        when(rulesService.listRates(10, null)).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/rules/commission-rates").param("codMarca", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codMarca").value(10));
    }

    @Test void listExceptions_byMonth_returnsOk() throws Exception {
        var ex = MonthlyException.builder().yearMonth("2025-07")
            .type(ExceptionType.ABSENCE).matricula("MATRIC-58").build();
        when(rulesService.listExceptions("2025-07", null, null)).thenReturn(List.of(ex));

        mockMvc.perform(get("/api/rules/exceptions").param("month", "2025-07"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].yearMonth").value("2025-07"))
            .andExpect(jsonPath("$[0].type").value("ABSENCE"));
    }

    @Test void listExceptions_missingMonth_returns400() throws Exception {
        mockMvc.perform(get("/api/rules/exceptions"))
            .andExpect(status().isBadRequest());
    }

    @Test void listExceptions_invalidMonthFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/rules/exceptions").param("month", "07-2025"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid month format. Use yyyy-MM"));
    }

    @Test void listExceptions_byMonthAndType_returnsOk() throws Exception {
        var ex = MonthlyException.builder().yearMonth("2025-07").type(ExceptionType.ABSENCE).matricula("MATRIC-58").build();
        when(rulesService.listExceptions("2025-07", ExceptionType.ABSENCE, null)).thenReturn(List.of(ex));

        mockMvc.perform(get("/api/rules/exceptions").param("month", "2025-07").param("type", "ABSENCE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("ABSENCE"));
    }

    @Test void listExceptions_byMonthAndMatricula_returnsOk() throws Exception {
        var ex = MonthlyException.builder().yearMonth("2025-07").type(ExceptionType.ABSENCE).matricula("MATRIC-58").build();
        when(rulesService.listExceptions("2025-07", null, "MATRIC-58")).thenReturn(List.of(ex));

        mockMvc.perform(get("/api/rules/exceptions").param("month", "2025-07").param("matricula", "MATRIC-58"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].matricula").value("MATRIC-58"));
    }

    @Test void listAllExceptions_returnsOk() throws Exception {
        var ex = MonthlyException.builder().yearMonth("2025-07").type(ExceptionType.ABSENCE).matricula("MATRIC-58").build();
        when(rulesService.listAllExceptions()).thenReturn(List.of(ex));

        mockMvc.perform(get("/api/rules/exceptions/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].yearMonth").value("2025-07"))
            .andExpect(jsonPath("$[0].type").value("ABSENCE"));
    }
}
