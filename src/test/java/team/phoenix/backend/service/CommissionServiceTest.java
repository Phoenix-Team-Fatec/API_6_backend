package team.phoenix.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionServiceTest {

    @Mock CommissionRateRepository rateRepo;
    @Mock HrRecordRepository hrRepo;
    @Mock SalesRecordRepository salesRepo;
    @Mock MonthlyExceptionRepository exceptionRepo;
    @Mock RestTemplate restTemplate;
    @InjectMocks CommissionServiceImpl service;

    private static final YearMonth JULY = YearMonth.of(2025, 7);

    @Test void simulate_delegatesToCalculator() {
        var hr = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY.atDay(1)).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        var sale = SalesRecord.builder().matricula("M1").vlrVenda(5000.0).build();

        when(hrRepo.findByMatriculaAndDataRef("M1", JULY.atDay(1))).thenReturn(Optional.of(hr));
        when(salesRepo.findByDateRef(JULY.atDay(1))).thenReturn(List.of(sale));
        when(rateRepo.findAll()).thenReturn(List.of(rate));
        when(exceptionRepo.findByYearMonth("2025-07")).thenReturn(List.of());
        when(restTemplate.postForObject(anyString(), any(), eq(List.class))).thenReturn(List.of(Map.of(
            "matricula", "M1",
            "base_vendas", 5000.0,
            "perc_comissao", 0.025,
            "valor_comissao_bruto", 125.0,
            "ajuste_proporcional", 1.0,
            "bonus", 0.0,
            "valor_final", 125.0
        )));

        CommissionResult result = service.simulate("M1", JULY);
        assertThat(result.matricula()).isEqualTo("M1");
        assertThat(result.month()).isEqualTo("2025-07");
        assertThat(result.finalCommission()).isEqualTo(125.0);
        assertThat(result.commissionRate()).isEqualTo(0.025);
    }

    @Test void simulate_throwsWhenHrNotFound() {
        when(hrRepo.findByMatriculaAndDataRef("X", JULY.atDay(1))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.simulate("X", JULY))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("HR record not found");
    }

    @Test void simulate_throwsWhenMLUnavailable() {
        var hr = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY.atDay(1)).dataAdmiss(LocalDate.of(2020,1,1)).build();
        when(hrRepo.findByMatriculaAndDataRef("M1", JULY.atDay(1))).thenReturn(Optional.of(hr));
        when(salesRepo.findByDateRef(JULY.atDay(1))).thenReturn(List.of());
        when(rateRepo.findAll()).thenReturn(List.of());
        when(exceptionRepo.findByYearMonth("2025-07")).thenReturn(List.of());
        when(restTemplate.postForObject(anyString(), any(), eq(List.class)))
            .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> service.simulate("M1", JULY))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("ML service unavailable");
    }
}
