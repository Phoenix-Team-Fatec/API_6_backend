package team.phoenix.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;
import java.time.LocalDate;
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

    private static final LocalDate JULY = LocalDate.of(2025, 7, 1);

    @Test void simulate_delegatesToCalculator() {
        var hr = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        var sale = SalesRecord.builder().matricula("M1").vlrVenda(5000.0).build();
        var expected = new CommissionResult("M1", JULY, hr,5000.0,0.025,125.0,List.of(),0.0,125.0,"GERAL","");

        when(hrRepo.findByMatriculaAndDataRef("M1", JULY)).thenReturn(Optional.of(hr));
        when(salesRepo.findByMatriculaAndDateRef("M1", JULY)).thenReturn(List.of(sale));
        when(salesRepo.findByCodLojaAndDateRef(35, JULY)).thenReturn(List.of(sale));
        when(rateRepo.findActiveLatestByCodMarcaAndCodCargo(10, 100)).thenReturn(Optional.of(rate));
        when(exceptionRepo.findByYearMonth(JULY)).thenReturn(List.of());
        when(calculator.calculate(hr, 5000.0, 5000.0, 0.025, List.of(), JULY)).thenReturn(expected);

        assertThat(service.simulate("M1", JULY)).isEqualTo(expected);
    }

    @Test void simulate_throwsWhenHrNotFound() {
        when(hrRepo.findByMatriculaAndDataRef("X", JULY)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.simulate("X", JULY))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("HR record not found");
    }

    @Test void simulate_throwsWhenRateNotFound() {
        var hr = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        when(hrRepo.findByMatriculaAndDataRef("M1", JULY)).thenReturn(Optional.of(hr));
        when(salesRepo.findByMatriculaAndDateRef("M1", JULY)).thenReturn(List.of());
        when(salesRepo.findByCodLojaAndDateRef(35, JULY)).thenReturn(List.of());
        when(rateRepo.findActiveLatestByCodMarcaAndCodCargo(10, 100)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.simulate("M1", JULY))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Commission rate not found");
    }

    @Test void calculate_employeeTarget_returnsSingleItemAggregate() {
        var hr = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        var sale = SalesRecord.builder().matricula("M1").vlrVenda(5000.0).build();
        var expected = new CommissionResult("M1", JULY, hr,5000.0,0.025,125.0,List.of(),0.0,125.0,"GERAL","");

        when(hrRepo.findByMatriculaAndDataRef("M1", JULY)).thenReturn(Optional.of(hr));
        when(salesRepo.findByMatriculaAndDateRef("M1", JULY)).thenReturn(List.of(sale));
        when(salesRepo.findByCodLojaAndDateRef(35, JULY)).thenReturn(List.of(sale));
        when(rateRepo.findActiveLatestByCodMarcaAndCodCargo(10, 100)).thenReturn(Optional.of(rate));
        when(exceptionRepo.findByYearMonth(JULY)).thenReturn(List.of());
        when(calculator.calculate(hr, 5000.0, 5000.0, 0.025, List.of(), JULY)).thenReturn(expected);

        var response = service.calculate(new CommissionCalculationCommand(
            "2025-07", CommissionTargetType.EMPLOYEE, "M1", null, null));

        assertThat(response.targetType()).isEqualTo(CommissionTargetType.EMPLOYEE);
        assertThat(response.targetId()).isEqualTo("M1");
        assertThat(response.items()).containsExactly(expected);
        assertThat(response.totalCommission()).isEqualTo(125.0);
        assertThat(response.appliedRules()).containsExactly("GERAL");
    }

    @Test void calculate_storeTarget_returnsEmployeesFromStore() {
        var hr1 = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var hr2 = HrRecord.builder().matricula("M2").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        var sale1 = SalesRecord.builder().matricula("M1").vlrVenda(5000.0).build();
        var sale2 = SalesRecord.builder().matricula("M2").vlrVenda(7000.0).build();
        var r1 = new CommissionResult("M1", JULY, hr1,5000.0,0.025,125.0,List.of(),0.0,125.0,"GERAL","");
        var r2 = new CommissionResult("M2", JULY, hr2,7000.0,0.025,175.0,List.of(),0.0,175.0,"GERAL","");

        when(hrRepo.findByCodLojaAndDataRef(35, JULY)).thenReturn(List.of(hr1, hr2));
        when(salesRepo.findByMatriculaAndDateRef("M1", JULY)).thenReturn(List.of(sale1));
        when(salesRepo.findByMatriculaAndDateRef("M2", JULY)).thenReturn(List.of(sale2));
        when(salesRepo.findByCodLojaAndDateRef(35, JULY)).thenReturn(List.of(sale1, sale2));
        when(rateRepo.findActiveLatestByCodMarcaAndCodCargo(10, 100)).thenReturn(Optional.of(rate));
        when(exceptionRepo.findByYearMonth(JULY)).thenReturn(List.of());
        when(calculator.calculate(hr1, 5000.0, 12000.0, 0.025, List.of(), JULY)).thenReturn(r1);
        when(calculator.calculate(hr2, 7000.0, 12000.0, 0.025, List.of(), JULY)).thenReturn(r2);

        var response = service.calculate(new CommissionCalculationCommand(
            "2025-07", CommissionTargetType.STORE, null, 35, null));

        assertThat(response.targetType()).isEqualTo(CommissionTargetType.STORE);
        assertThat(response.targetId()).isEqualTo("35");
        assertThat(response.items()).containsExactly(r1, r2);
        assertThat(response.totalCommission()).isEqualTo(300.0);
    }

    @Test void calculate_brandTarget_returnsEmployeesFromBrand() {
        var hr1 = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var hr2 = HrRecord.builder().matricula("M2").codMarca(10).codLoja(36).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        var sale1 = SalesRecord.builder().matricula("M1").vlrVenda(5000.0).build();
        var sale2 = SalesRecord.builder().matricula("M2").vlrVenda(7000.0).build();
        var r1 = new CommissionResult("M1", JULY, hr1,5000.0,0.025,125.0,List.of(),0.0,125.0,"GERAL","");
        var r2 = new CommissionResult("M2", JULY, hr2,7000.0,0.025,175.0,List.of(),0.0,175.0,"GERAL","");

        when(hrRepo.findByCodMarcaAndDataRef(10, JULY)).thenReturn(List.of(hr1, hr2));
        when(salesRepo.findByMatriculaAndDateRef("M1", JULY)).thenReturn(List.of(sale1));
        when(salesRepo.findByMatriculaAndDateRef("M2", JULY)).thenReturn(List.of(sale2));
        when(salesRepo.findByCodLojaAndDateRef(35, JULY)).thenReturn(List.of(sale1));
        when(salesRepo.findByCodLojaAndDateRef(36, JULY)).thenReturn(List.of(sale2));
        when(rateRepo.findActiveLatestByCodMarcaAndCodCargo(10, 100)).thenReturn(Optional.of(rate));
        when(exceptionRepo.findByYearMonth(JULY)).thenReturn(List.of());
        when(calculator.calculate(hr1, 5000.0, 5000.0, 0.025, List.of(), JULY)).thenReturn(r1);
        when(calculator.calculate(hr2, 7000.0, 7000.0, 0.025, List.of(), JULY)).thenReturn(r2);

        var response = service.calculate(new CommissionCalculationCommand(
            "2025-07", CommissionTargetType.BRAND, null, null, 10));

        assertThat(response.targetType()).isEqualTo(CommissionTargetType.BRAND);
        assertThat(response.targetId()).isEqualTo("10");
        assertThat(response.items()).containsExactly(r1, r2);
        assertThat(response.totalCommission()).isEqualTo(300.0);
    }

    @Test void calculate_rejectsMissingTargetFields() {
        assertThatThrownBy(() -> service.calculate(new CommissionCalculationCommand(
            "2025-07", null, null, null, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("targetType is required");

        assertThatThrownBy(() -> service.calculate(new CommissionCalculationCommand(
            "2025-07", CommissionTargetType.EMPLOYEE, null, null, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("matricula is required");

        assertThatThrownBy(() -> service.calculate(new CommissionCalculationCommand(
            "2025-07", CommissionTargetType.STORE, null, null, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("codLoja is required");

        assertThatThrownBy(() -> service.calculate(new CommissionCalculationCommand(
            "2025-07", CommissionTargetType.BRAND, null, null, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("codMarca is required");
    }

    @Test void simulate_managerWithMultiStoreException_addsProportionalAlternateStoreSales() {
        var manager = HrRecord.builder().matricula("MATRIC-293").codMarca(10).codLoja(1).codCargo(150)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder().codMarca(10).codCargo(150).pctComiss(0.01).build();
        var mainSale = SalesRecord.builder().codLoja(1).vlrVenda(90000.0).build();
        var alternateSale = SalesRecord.builder().codLoja(5).vlrVenda(62000.0).build();
        var multiStore = MonthlyException.builder().yearMonth(JULY).type(ExceptionType.MULTI_STORE)
            .matricula("MATRIC-293").alternateCodLoja(5).daysWorked(10).build();
        double adjustedStoreSales = 90000.0 + (62000.0 * 10.0 / 31.0);
        var expected = new CommissionResult("MATRIC-293", JULY, manager, adjustedStoreSales,0.01,
            adjustedStoreSales * 0.01,List.of(),0.0,adjustedStoreSales * 0.01,"GERAL","");

        when(hrRepo.findByMatriculaAndDataRef("MATRIC-293", JULY)).thenReturn(Optional.of(manager));
        when(salesRepo.findByMatriculaAndDateRef("MATRIC-293", JULY)).thenReturn(List.of());
        when(salesRepo.findByCodLojaAndDateRef(1, JULY)).thenReturn(List.of(mainSale));
        when(salesRepo.findByCodLojaAndDateRef(5, JULY)).thenReturn(List.of(alternateSale));
        when(rateRepo.findActiveLatestByCodMarcaAndCodCargo(10, 150)).thenReturn(Optional.of(rate));
        when(exceptionRepo.findByYearMonth(JULY)).thenReturn(List.of(multiStore));
        when(calculator.calculate(manager, 0.0, adjustedStoreSales, 0.01, List.of(multiStore), JULY))
            .thenReturn(expected);

        assertThat(service.simulate("MATRIC-293", JULY)).isEqualTo(expected);
    }
}
