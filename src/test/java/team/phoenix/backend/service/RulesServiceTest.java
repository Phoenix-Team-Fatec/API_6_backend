package team.phoenix.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RulesServiceTest {

    @Mock CommissionRateRepository rateRepo;
    @Mock MonthlyExceptionRepository exceptionRepo;
    @InjectMocks RulesServiceImpl service;

    @Test void listRates_noFilter_returnsAll() {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        when(rateRepo.findAll()).thenReturn(List.of(rate));
        assertThat(service.listRates(null, null)).hasSize(1);
    }

    @Test void listRates_filterByCodMarca_returnsFiltered() {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        when(rateRepo.findByCodMarca(10)).thenReturn(List.of(rate));
        assertThat(service.listRates(10, null)).hasSize(1);
        verify(rateRepo).findByCodMarca(10);
    }

    @Test void listRates_filterByCodCargo_returnsFiltered() {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        when(rateRepo.findByCodCargo(100)).thenReturn(List.of(rate));
        assertThat(service.listRates(null, 100)).hasSize(1);
        verify(rateRepo).findByCodCargo(100);
    }

    @Test void listRates_filterByBoth_returnsFiltered() {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        when(rateRepo.findByCodMarcaAndCodCargo(10, 100)).thenReturn(Optional.of(rate));
        assertThat(service.listRates(10, 100)).hasSize(1);
        verify(rateRepo).findByCodMarcaAndCodCargo(10, 100);
    }

    @Test void listExceptions_byMonth_returnsAll() {
        var ex = MonthlyException.builder().yearMonth("2025-07").type(ExceptionType.ABSENCE).build();
        when(exceptionRepo.findByYearMonth("2025-07")).thenReturn(List.of(ex));
        assertThat(service.listExceptions("2025-07", null, null)).hasSize(1);
    }

    @Test void listExceptions_byMonthAndType_returnsFiltered() {
        var ex = MonthlyException.builder().yearMonth("2025-07").type(ExceptionType.ABSENCE).build();
        when(exceptionRepo.findByYearMonthAndType("2025-07", ExceptionType.ABSENCE)).thenReturn(List.of(ex));
        assertThat(service.listExceptions("2025-07", ExceptionType.ABSENCE, null)).hasSize(1);
    }

    @Test void listExceptions_byMonthAndMatricula_returnsFiltered() {
        var ex = MonthlyException.builder().yearMonth("2025-07").matricula("MATRIC-58").build();
        when(exceptionRepo.findByYearMonthAndMatricula("2025-07", "MATRIC-58")).thenReturn(List.of(ex));
        assertThat(service.listExceptions("2025-07", null, "MATRIC-58")).hasSize(1);
    }

    @Test void listExceptions_byMonthTypeAndMatricula_returnsFiltered() {
        var ex = MonthlyException.builder().yearMonth("2025-07").type(ExceptionType.ABSENCE).matricula("MATRIC-58").build();
        when(exceptionRepo.findByYearMonthAndTypeAndMatricula("2025-07", ExceptionType.ABSENCE, "MATRIC-58")).thenReturn(List.of(ex));
        assertThat(service.listExceptions("2025-07", ExceptionType.ABSENCE, "MATRIC-58")).hasSize(1);
        verify(exceptionRepo).findByYearMonthAndTypeAndMatricula("2025-07", ExceptionType.ABSENCE, "MATRIC-58");
    }
}
