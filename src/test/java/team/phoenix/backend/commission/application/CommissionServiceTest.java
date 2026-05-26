package team.phoenix.backend.commission.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.phoenix.backend.commission.domain.CommissionCalculator;
import team.phoenix.backend.commission.domain.CommissionResult;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    @Mock CommissionAiClient aiClient;
    @InjectMocks CommissionServiceImpl service;

    private static final LocalDate JULY = LocalDate.of(2025, 7, 1);

    @Test void simulate_delegatesToCalculator() {
        var hr = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder()
            .id("rate-1")
            .nomeRegra("Comissao PRETO Julho")
            .codMarca(10)
            .descrMarca("PRETO")
            .codCargo(100)
            .descriCargo("VENDEDOR")
            .pctComiss(0.025)
            .textoOriginal("Regra PRETO")
            .build();
        var sale = SalesRecord.builder().matricula("M1").vlrVenda(5000.0).build();
        var expected = new CommissionResult("M1", JULY, hr,5000.0,0.025,125.0,List.of(),0.0,125.0,"GERAL","");

        when(hrRepo.findByMatriculaAndDataRef("M1", JULY)).thenReturn(Optional.of(hr));
        when(salesRepo.findByMatriculaAndDateRef("M1", JULY)).thenReturn(List.of(sale));
        when(salesRepo.findByCodLojaAndDateRef(35, JULY)).thenReturn(List.of(sale));
        when(rateRepo.findFirstByCodMarcaAndCodCargoAndIsVigenteTrueAndDeletedAtNullOrderByVersaoDesc(10, 100)).thenReturn(Optional.of(rate));
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
        when(rateRepo.findFirstByCodMarcaAndCodCargoAndIsVigenteTrueAndDeletedAtNullOrderByVersaoDesc(10, 100)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.simulate("M1", JULY))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Commission rate not found");
    }

    @Test void calculate_employeeTarget_delegatesCalculationToAi() {
        var hr = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .descrMarca("PRETO").descrLoja("LOJA-35").descriCargo("VENDEDOR")
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder()
            .id("rate-1")
            .nomeRegra("Comissao PRETO Julho")
            .codMarca(10)
            .descrMarca("PRETO")
            .codCargo(100)
            .descriCargo("VENDEDOR")
            .pctComiss(0.025)
            .textoOriginal("Regra PRETO")
            .build();
        var sale = SalesRecord.builder().matricula("M1").codMarca(10).codLoja(35).vlrVenda(5000.0).build();

        when(hrRepo.findByMatriculaAndDataRef("M1", JULY)).thenReturn(Optional.of(hr));
        when(salesRepo.findByCodLojaAndDateRef(35, JULY)).thenReturn(List.of(sale));
        when(rateRepo.findFirstByCodMarcaAndCodCargoAndIsVigenteTrueAndDeletedAtNullOrderByVersaoDesc(10, 100)).thenReturn(Optional.of(rate));
        when(exceptionRepo.findByYearMonth(JULY)).thenReturn(List.of());
        when(aiClient.calculate(any(), eq(2025), eq(7))).thenReturn(List.of(
            new AiCommissionResult("M1", "35", 10, 5000.0, 0.025, 125.0, 1.0, 0.0, 125.0)
        ));

        var response = service.calculate(new CommissionCalculationCommand(
            "2025-07", CommissionTargetType.EMPLOYEE, "M1", null, null));

        assertThat(response.targetType()).isEqualTo(CommissionTargetType.EMPLOYEE);
        assertThat(response.targetId()).isEqualTo("M1");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).employee()).isEqualTo(hr);
        assertThat(response.items().get(0).salesBase()).isEqualTo(5000.0);
        assertThat(response.items().get(0).commissionRate()).isEqualTo(0.025);
        assertThat(response.items().get(0).finalCommission()).isEqualTo(125.0);
        assertThat(response.totalCommission()).isEqualTo(125.0);
        assertThat(response.appliedRules()).containsExactly("IA_COMMISSION_ALGORITHM");
        assertThat(response.appliedRuleDetails()).hasSize(1);
        assertThat(response.appliedRuleDetails().get(0).id()).isEqualTo("rate-1");
        assertThat(response.appliedRuleDetails().get(0).nomeRegra()).isEqualTo("Comissao PRETO Julho");
        assertThat(response.appliedRuleDetails().get(0).tipo()).isEqualTo("COMMISSION_RATE");
        assertThat(response.appliedRuleDetails().get(0).descricao()).isEqualTo("Regra PRETO");

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(AiCommissionRequest.class);
        verify(aiClient).calculate(requestCaptor.capture(), eq(2025), eq(7));
        var aiRequest = requestCaptor.getValue();
        assertThat(aiRequest.funcionarios()).hasSize(1);
        assertThat(aiRequest.funcionarios().get(0).matricula()).isEqualTo("M1");
        assertThat(aiRequest.vendas()).hasSize(1);
        assertThat(aiRequest.vendas().get(0).vlrVenda()).isEqualTo(5000.0);
        assertThat(aiRequest.tabelaComissao()).hasSize(1);
        assertThat(aiRequest.tabelaComissao().get(0).percComissao()).isEqualTo(0.025);
        verifyNoInteractions(calculator);
    }

    @Test void calculate_includesCommissionRatesFromCalculationMonthAsAiOverrides() {
        var hr = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .descrMarca("PRETO").descrLoja("LOJA-35").descriCargo("VENDEDOR")
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var monthlyOverride = CommissionRate.builder()
            .codMarca(10)
            .codCargo(100)
            .pctComiss(0.06)
            .data(JULY)
            .textoOriginal("Regra IA")
            .build();

        when(hrRepo.findByMatriculaAndDataRef("M1", JULY)).thenReturn(Optional.of(hr));
        when(salesRepo.findByCodLojaAndDateRef(35, JULY)).thenReturn(List.of());
        when(rateRepo.findByDataAndIsVigenteTrueAndDeletedAtNull(JULY)).thenReturn(List.of(monthlyOverride));
        when(exceptionRepo.findByYearMonth(JULY)).thenReturn(List.of());
        when(aiClient.calculate(any(), eq(2025), eq(7))).thenReturn(List.of(
            new AiCommissionResult("M1", "35", 10, 0.0, 0.06, 0.0, 1.0, 0.0, 0.0)
        ));

        service.calculate(new CommissionCalculationCommand(
            "2025-07", CommissionTargetType.EMPLOYEE, "M1", null, null));

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(AiCommissionRequest.class);
        verify(aiClient).calculate(requestCaptor.capture(), eq(2025), eq(7));
        var regrasMongo = requestCaptor.getValue().regrasMongo();

        assertThat(regrasMongo).hasSize(1);
        assertThat(regrasMongo.get(0)).containsEntry("tipo", "override");
        assertThat(regrasMongo.get(0).get("override")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        var override = (Map<String, Object>) regrasMongo.get(0).get("override");
        assertThat(override).containsEntry("data_inicio", JULY);
        assertThat(override).containsEntry("data_fim", LocalDate.of(2025, 7, 31));
        @SuppressWarnings("unchecked")
        var percOverride = (Map<String, Object>) override.get("perc_override");
        assertThat(percOverride).containsEntry("10,100", 0.06);
        assertThat(requestCaptor.getValue().tabelaComissao()).hasSize(1);
        assertThat(requestCaptor.getValue().tabelaComissao().get(0).percComissao()).isEqualTo(0.06);
    }

    @Test void calculate_storeTarget_returnsEmployeesFromStore() {
        var hr1 = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var hr2 = HrRecord.builder().matricula("M2").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        var sale1 = SalesRecord.builder().matricula("M1").codMarca(10).codLoja(35).vlrVenda(5000.0).build();
        var sale2 = SalesRecord.builder().matricula("M2").codMarca(10).codLoja(35).vlrVenda(7000.0).build();

        when(hrRepo.findByCodLojaAndDataRef(35, JULY)).thenReturn(List.of(hr1, hr2));
        when(salesRepo.findByCodLojaAndDateRef(35, JULY)).thenReturn(List.of(sale1, sale2));
        when(rateRepo.findFirstByCodMarcaAndCodCargoAndIsVigenteTrueAndDeletedAtNullOrderByVersaoDesc(10, 100)).thenReturn(Optional.of(rate));
        when(exceptionRepo.findByYearMonth(JULY)).thenReturn(List.of());
        when(aiClient.calculate(any(), eq(2025), eq(7))).thenReturn(List.of(
            new AiCommissionResult("M1", "35", 10, 5000.0, 0.025, 125.0, 1.0, 0.0, 125.0),
            new AiCommissionResult("M2", "35", 10, 7000.0, 0.025, 175.0, 1.0, 0.0, 175.0)
        ));

        var response = service.calculate(new CommissionCalculationCommand(
            "2025-07", CommissionTargetType.STORE, null, 35, null));

        assertThat(response.targetType()).isEqualTo(CommissionTargetType.STORE);
        assertThat(response.targetId()).isEqualTo("35");
        assertThat(response.items()).extracting(CommissionResult::matricula).containsExactly("M1", "M2");
        assertThat(response.totalCommission()).isEqualTo(300.0);
    }

    @Test void calculate_brandTarget_returnsEmployeesFromBrand() {
        var hr1 = HrRecord.builder().matricula("M1").codMarca(10).codLoja(35).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var hr2 = HrRecord.builder().matricula("M2").codMarca(10).codLoja(36).codCargo(100)
            .dataRef(JULY).dataAdmiss(LocalDate.of(2020,1,1)).build();
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).build();
        var sale1 = SalesRecord.builder().matricula("M1").codMarca(10).codLoja(35).vlrVenda(5000.0).build();
        var sale2 = SalesRecord.builder().matricula("M2").codMarca(10).codLoja(36).vlrVenda(7000.0).build();

        when(hrRepo.findByCodMarcaAndDataRef(10, JULY)).thenReturn(List.of(hr1, hr2));
        when(salesRepo.findByCodLojaAndDateRef(35, JULY)).thenReturn(List.of(sale1));
        when(salesRepo.findByCodLojaAndDateRef(36, JULY)).thenReturn(List.of(sale2));
        when(rateRepo.findFirstByCodMarcaAndCodCargoAndIsVigenteTrueAndDeletedAtNullOrderByVersaoDesc(10, 100)).thenReturn(Optional.of(rate));
        when(exceptionRepo.findByYearMonth(JULY)).thenReturn(List.of());
        when(aiClient.calculate(any(), eq(2025), eq(7))).thenReturn(List.of(
            new AiCommissionResult("M1", "35", 10, 5000.0, 0.025, 125.0, 1.0, 0.0, 125.0),
            new AiCommissionResult("M2", "36", 10, 7000.0, 0.025, 175.0, 1.0, 0.0, 175.0)
        ));

        var response = service.calculate(new CommissionCalculationCommand(
            "2025-07", CommissionTargetType.BRAND, null, null, 10));

        assertThat(response.targetType()).isEqualTo(CommissionTargetType.BRAND);
        assertThat(response.targetId()).isEqualTo("10");
        assertThat(response.items()).extracting(CommissionResult::matricula).containsExactly("M1", "M2");
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
        when(rateRepo.findFirstByCodMarcaAndCodCargoAndIsVigenteTrueAndDeletedAtNullOrderByVersaoDesc(10, 150)).thenReturn(Optional.of(rate));
        when(exceptionRepo.findByYearMonth(JULY)).thenReturn(List.of(multiStore));
        when(calculator.calculate(manager, 0.0, adjustedStoreSales, 0.01, List.of(multiStore), JULY))
            .thenReturn(expected);

        assertThat(service.simulate("MATRIC-293", JULY)).isEqualTo(expected);
    }
}
