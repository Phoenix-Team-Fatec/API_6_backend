package team.phoenix.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).isVigente(true).build();
        when(rateRepo.findAll()).thenReturn(List.of(rate));
        assertThat(service.listRates(null, null, null)).hasSize(1);
    }

    @Test void listRates_filterByCodMarca_returnsFiltered() {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).isVigente(true).build();
        when(rateRepo.findByCodMarca(10)).thenReturn(List.of(rate));
        assertThat(service.listRates(10, null, null)).hasSize(1);
        verify(rateRepo).findByCodMarca(10);
    }

    @Test void listRates_filterByCodCargo_returnsFiltered() {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).isVigente(true).build();
        when(rateRepo.findByCodCargo(100)).thenReturn(List.of(rate));
        assertThat(service.listRates(null, 100, null)).hasSize(1);
        verify(rateRepo).findByCodCargo(100);
    }

    @Test void listRates_filterByBoth_returnsFiltered() {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).isVigente(true).build();
        when(rateRepo.findByCodMarcaAndCodCargo(10, 100)).thenReturn(Optional.of(rate));
        assertThat(service.listRates(10, 100, null)).hasSize(1);
        verify(rateRepo).findByCodMarcaAndCodCargo(10, 100);
    }

    @Test void listRates_withIsVigenteTrue_returnsOnlyActive() {
        var active = CommissionRate.builder().isVigente(true).build();
        var inactive = CommissionRate.builder().isVigente(false).build();
        when(rateRepo.findAll()).thenReturn(List.of(active, inactive));

        assertThat(service.listRates(null, null, true)).containsExactly(active);
    }

    @Test void listRates_withIsVigenteFalse_returnsOnlyInactive() {
        var active = CommissionRate.builder().isVigente(true).build();
        var inactive = CommissionRate.builder().isVigente(false).build();
        when(rateRepo.findAll()).thenReturn(List.of(active, inactive));

        assertThat(service.listRates(null, null, false)).containsExactly(inactive);
    }

    @Test void listExceptions_byMonth_returnsAll() {
        var ex = MonthlyException.builder().yearMonth(LocalDate.of(2025,7,1)).type(ExceptionType.ABSENCE).build();
        when(exceptionRepo.findByYearMonth(LocalDate.of(2025,7,1))).thenReturn(List.of(ex));
        assertThat(service.listExceptions(LocalDate.of(2025,7,1), null, null)).hasSize(1);
    }

    @Test void listExceptions_byMonthAndType_returnsFiltered() {
        var ex = MonthlyException.builder().yearMonth(LocalDate.of(2025,7,1)).type(ExceptionType.ABSENCE).build();
        when(exceptionRepo.findByYearMonthAndType(LocalDate.of(2025,7,1), ExceptionType.ABSENCE)).thenReturn(List.of(ex));
        assertThat(service.listExceptions(LocalDate.of(2025,7,1), ExceptionType.ABSENCE, null)).hasSize(1);
    }

    @Test void listExceptions_byMonthAndMatricula_returnsFiltered() {
        var ex = MonthlyException.builder().yearMonth(LocalDate.of(2025,7,1)).matricula("MATRIC-58").build();
        when(exceptionRepo.findByYearMonthAndMatricula(LocalDate.of(2025,7,1), "MATRIC-58")).thenReturn(List.of(ex));
        assertThat(service.listExceptions(LocalDate.of(2025,7,1), null, "MATRIC-58")).hasSize(1);
    }

    @Test void listExceptions_byMonthTypeAndMatricula_returnsFiltered() {
        var ex = MonthlyException.builder().yearMonth(LocalDate.of(2025,7,1)).type(ExceptionType.ABSENCE).matricula("MATRIC-58").build();
        when(exceptionRepo.findByYearMonthAndTypeAndMatricula(LocalDate.of(2025,7,1), ExceptionType.ABSENCE, "MATRIC-58")).thenReturn(List.of(ex));
        assertThat(service.listExceptions(LocalDate.of(2025,7,1), ExceptionType.ABSENCE, "MATRIC-58")).hasSize(1);
        verify(exceptionRepo).findByYearMonthAndTypeAndMatricula(LocalDate.of(2025,7,1), ExceptionType.ABSENCE, "MATRIC-58");
    }

    @Test void activateRate_whenNotDeleted_reattivatesRule() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(false)
            .deletedAt(null)
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        service.activateRate("123");

        assertThat(rate.getIsVigente()).isTrue();
        verify(rateRepo).save(rate);
    }

    @Test void activateRate_whenDeleted_throwsIllegalStateException() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(false)
            .deletedAt(LocalDateTime.now())
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        assertThatThrownBy(() -> service.activateRate("123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Regra removida não pode ser reativada");
        verify(rateRepo, never()).save(any());
    }

    @Test void deactivateRate_whenFound_marksAsDeletedAndInactive() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(true)
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        service.deactivateRate("123");

        assertThat(rate.getIsVigente()).isFalse();
        assertThat(rate.getDeletedAt()).isNotNull();
        verify(rateRepo).save(rate);
    }

    @Test void restoreRate_whenDeleted_recoversWithoutActivating() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(false)
            .deletedAt(LocalDateTime.now())
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        service.restoreRate("123");

        assertThat(rate.getDeletedAt()).isNull();
        assertThat(rate.getIsVigente()).isFalse();
        verify(rateRepo).save(rate);
    }

    @Test void restoreRate_whenNotDeleted_throwsIllegalStateException() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(true)
            .deletedAt(null)
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        assertThatThrownBy(() -> service.restoreRate("123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Regra não foi deletada");
        verify(rateRepo, never()).save(any());
    }
}
