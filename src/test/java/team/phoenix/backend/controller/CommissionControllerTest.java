package team.phoenix.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team.phoenix.backend.domain.model.HrRecord;
import team.phoenix.backend.service.CommissionCalculationResult;
import team.phoenix.backend.service.CommissionResult;
import team.phoenix.backend.service.CommissionService;
import team.phoenix.backend.service.CommissionTargetType;
import team.phoenix.backend.service.AppliedRuleDetail;
import team.phoenix.backend.service.exception.CommissionRateNotFoundException;
import team.phoenix.backend.service.exception.EmployeeNotFoundException;
import team.phoenix.backend.service.exception.InvalidCommissionRequestException;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommissionController.class)
class CommissionControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CommissionService commissionService;

    @Test void simulate_returnsOk() throws Exception {
        var hr = HrRecord.builder().matricula("MATRIC-1").codMarca(10).descrMarca("PRETO")
            .codLoja(35).descrLoja("LOJA-35").codCargo(100).descriCargo("VENDEDOR LOJA")
            .dataRef(LocalDate.of(2025,7,1)).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var result = new CommissionResult("MATRIC-1", LocalDate.of(2025,7,1), hr,5000.0,0.025,125.0,List.of(),0.0,125.0,"GERAL","5000 x 0.025 = 125");
        when(commissionService.simulate("MATRIC-1", LocalDate.of(2025,7,1))).thenReturn(result);

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
        when(commissionService.simulate("MATRIC-999", LocalDate.of(2025,7,1)))
            .thenThrow(new EmployeeNotFoundException("HR record not found"));
        mockMvc.perform(get("/api/commission/simulate")
                .param("matricula","MATRIC-999").param("month","2025-07"))
            .andExpect(status().isNotFound());
    }

    @Test void simulate_returns400ForBadMonthFormat() throws Exception {
        mockMvc.perform(get("/api/commission/simulate")
                .param("matricula","MATRIC-1").param("month","07-2025"))
            .andExpect(status().isBadRequest());
    }

    @Test void calculate_employeeTarget_returnsOk() throws Exception {
        var hr = HrRecord.builder().matricula("MATRIC-1").codMarca(10).descrMarca("PRETO")
            .codLoja(35).descrLoja("LOJA-35").codCargo(100).descriCargo("VENDEDOR LOJA")
            .dataRef(LocalDate.of(2025,7,1)).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var item = new CommissionResult("MATRIC-1", LocalDate.of(2025,7,1), hr,
            5000.0,0.025,125.0,List.of(),0.0,125.0,"GERAL","5000 x 0.025 = 125");
        var result = CommissionCalculationResult.from(LocalDate.of(2025,7,1),
            CommissionTargetType.EMPLOYEE, "MATRIC-1", List.of(item), List.of(
                new AppliedRuleDetail("rate-1", "Comissao PRETO Julho", "COMMISSION_RATE", "Regra PRETO")
            ));
        when(commissionService.calculate(any())).thenReturn(result);

        mockMvc.perform(post("/api/commission/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"month\":\"2025-07\",\"targetType\":\"EMPLOYEE\",\"matricula\":\"MATRIC-1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.targetType").value("EMPLOYEE"))
            .andExpect(jsonPath("$.targetId").value("MATRIC-1"))
            .andExpect(jsonPath("$.items[0].matricula").value("MATRIC-1"))
            .andExpect(jsonPath("$.totalCommission").value(125.0))
            .andExpect(jsonPath("$.appliedRules[0]").value("GERAL"))
            .andExpect(jsonPath("$.appliedRuleDetails[0].id").value("rate-1"))
            .andExpect(jsonPath("$.appliedRuleDetails[0].nomeRegra").value("Comissao PRETO Julho"))
            .andExpect(jsonPath("$.appliedRuleDetails[0].tipo").value("COMMISSION_RATE"));
    }

    @Test void calculate_storeTarget_returnsOk() throws Exception {
        var result = new CommissionCalculationResult(LocalDate.of(2025,7,1),
            CommissionTargetType.STORE, "35", List.of(), 0.0, List.of());
        when(commissionService.calculate(any())).thenReturn(result);

        mockMvc.perform(post("/api/commission/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"month\":\"2025-07\",\"targetType\":\"STORE\",\"codLoja\":35}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.targetType").value("STORE"))
            .andExpect(jsonPath("$.targetId").value("35"));
    }

    @Test void calculate_brandTarget_returnsOk() throws Exception {
        var result = new CommissionCalculationResult(LocalDate.of(2025,7,1),
            CommissionTargetType.BRAND, "10", List.of(), 0.0, List.of());
        when(commissionService.calculate(any())).thenReturn(result);

        mockMvc.perform(post("/api/commission/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"month\":\"2025-07\",\"targetType\":\"BRAND\",\"codMarca\":10}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.targetType").value("BRAND"))
            .andExpect(jsonPath("$.targetId").value("10"));
    }

    @Test void calculate_returns400ForBadMonthFormat() throws Exception {
        mockMvc.perform(post("/api/commission/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"month\":\"07-2025\",\"targetType\":\"EMPLOYEE\",\"matricula\":\"MATRIC-1\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid month format. Use yyyy-MM"));
    }

    @Test void calculate_returns400ForInvalidRequest() throws Exception {
        when(commissionService.calculate(any()))
            .thenThrow(new InvalidCommissionRequestException("matricula is required for EMPLOYEE target"));

        mockMvc.perform(post("/api/commission/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"month\":\"2025-07\",\"targetType\":\"EMPLOYEE\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("matricula is required for EMPLOYEE target"));
    }

    @Test void calculate_returns404ForMissingEmployeeOrRate() throws Exception {
        doThrow(new EmployeeNotFoundException("No HR records found"))
            .when(commissionService).calculate(any());

        mockMvc.perform(post("/api/commission/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"month\":\"2025-07\",\"targetType\":\"EMPLOYEE\",\"matricula\":\"MATRIC-999\"}"))
            .andExpect(status().isNotFound());

        reset(commissionService);
        doThrow(new CommissionRateNotFoundException("Commission rate not found"))
            .when(commissionService).calculate(any());

        mockMvc.perform(post("/api/commission/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"month\":\"2025-07\",\"targetType\":\"EMPLOYEE\",\"matricula\":\"MATRIC-1\"}"))
            .andExpect(status().isNotFound());
    }
}
