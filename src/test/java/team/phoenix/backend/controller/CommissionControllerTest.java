package team.phoenix.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team.phoenix.backend.domain.model.HrRecord;
import team.phoenix.backend.service.CommissionResult;
import team.phoenix.backend.service.CommissionService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommissionController.class)
class CommissionControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CommissionService commissionService;

    @Test void simulate_returnsOk() throws Exception {
        var hr = HrRecord.builder().matricula("MATRIC-1").codMarca(10).descrMarca("PRETO")
            .codLoja(35).descrLoja("LOJA-35").codCargo(100).descriCargo("VENDEDOR LOJA")
            .dataRef(LocalDate.of(2025,7,1)).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var result = new CommissionResult("MATRIC-1","2025-07",hr,5000.0,0.025,125.0,List.of(),0.0,125.0,"GERAL","5000 x 0.025 = 125");
        when(commissionService.simulate("MATRIC-1", YearMonth.of(2025,7))).thenReturn(result);

        mockMvc.perform(get("/api/commission/simulate")
                .param("matricula","MATRIC-1").param("month","2025-07"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.matricula").value("MATRIC-1"))
            .andExpect(jsonPath("$.finalCommission").value(125.0))
            .andExpect(jsonPath("$.ruleApplied").value("GERAL"))
            .andExpect(jsonPath("$.employee.codMarca").value(10))
            .andExpect(jsonPath("$.employee.descrMarca").value("PRETO"));
    }

    @Test void simulate_returns404WhenNotFound() throws Exception {
        when(commissionService.simulate("MATRIC-999", YearMonth.of(2025,7)))
            .thenThrow(new RuntimeException("HR record not found"));
        mockMvc.perform(get("/api/commission/simulate")
                .param("matricula","MATRIC-999").param("month","2025-07"))
            .andExpect(status().isNotFound());
    }

    @Test void simulate_returns400ForBadMonthFormat() throws Exception {
        mockMvc.perform(get("/api/commission/simulate")
                .param("matricula","MATRIC-1").param("month","07-2025"))
            .andExpect(status().isBadRequest());
    }
}
