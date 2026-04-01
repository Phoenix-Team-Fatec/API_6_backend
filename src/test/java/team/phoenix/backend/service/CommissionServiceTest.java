package team.phoenix.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionServiceTest {

    @Mock CommissionRateRepository rateRepo;
    @Mock HrRecordRepository hrRepo;
    @Mock SalesRecordRepository salesRepo;
    @Mock MonthlyExceptionRepository exceptionRepo;
    @Mock CommissionCalculator calculator;
    @InjectMocks CommissionServiceImpl service;

    private static final YearMonth JULY = YearMonth.of(2025, 7);

    @Test void simulate_delegatesToCalculator() {
        var hr = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY.atDay(1)).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        var sale = SalesRecord.builder().matricula("M1").vlrVenda(5000.0).build();
        var expected = new CommissionResult("M1","2025-07",hr,5000.0,0.025,125.0,List.of(),0.0,125.0,"GERAL","");

        when(hrRepo.findByMatriculaAndDataRef("M1", JULY.atDay(1))).thenReturn(Optional.of(hr));
        when(salesRepo.findByMatriculaAndDateRef("M1", JULY.atDay(1))).thenReturn(List.of(sale));
        when(salesRepo.findByCodLojaAndDateRef(35, JULY.atDay(1))).thenReturn(List.of(sale));
        when(rateRepo.findByCodMarcaAndCodCargo(10, 100)).thenReturn(Optional.of(rate));
        when(exceptionRepo.findByYearMonth("2025-07")).thenReturn(List.of());
        when(calculator.calculate(hr, 5000.0, 5000.0, 0.025, List.of(), JULY)).thenReturn(expected);

        assertThat(service.simulate("M1", JULY)).isEqualTo(expected);
    }

    @Test void simulate_throwsWhenHrNotFound() {
        when(hrRepo.findByMatriculaAndDataRef("X", JULY.atDay(1))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.simulate("X", JULY))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("HR record not found");
    }

    @Test void simulate_throwsWhenRateNotFound() {
        var hr = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY.atDay(1)).dataAdmiss(LocalDate.of(2020,1,1)).build();
        when(hrRepo.findByMatriculaAndDataRef("M1", JULY.atDay(1))).thenReturn(Optional.of(hr));
        when(salesRepo.findByMatriculaAndDateRef("M1", JULY.atDay(1))).thenReturn(List.of());
        when(salesRepo.findByCodLojaAndDateRef(35, JULY.atDay(1))).thenReturn(List.of());
        when(rateRepo.findByCodMarcaAndCodCargo(10, 100)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.simulate("M1", JULY))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Commission rate not found");
    }
}
