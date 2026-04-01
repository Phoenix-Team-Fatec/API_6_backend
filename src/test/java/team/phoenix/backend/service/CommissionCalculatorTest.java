package team.phoenix.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team.phoenix.backend.domain.model.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class CommissionCalculatorTest {

    private CommissionCalculator calculator;
    private static final YearMonth JULY = YearMonth.of(2025, 7); // 31 days
    private static final double D = 0.01;

    @BeforeEach void setUp() { calculator = new CommissionCalculatorImpl(); }

    private HrRecord hr(String matricula, int codMarca, int codLoja, int codCargo,
                        LocalDate admiss, LocalDate demiss) {
        return HrRecord.builder().matricula(matricula).codMarca(codMarca)
            .descrMarca("M").codLoja(codLoja).descrLoja("L").codCargo(codCargo)
            .descriCargo("C").dataRef(JULY.atDay(1)).dataAdmiss(admiss).dataDemiss(demiss).build();
    }

    @Test void geral_nonManager_usesIndividualSales() {
        var r = calculator.calculate(hr("M1", 10, 35, 100, LocalDate.of(2020,1,1), null),
            5000.0, 80000.0, 0.025, List.of(), JULY);
        assertThat(r.commissionBase()).isCloseTo(125.0, within(D));
        assertThat(r.ruleApplied()).isEqualTo("GERAL");
    }

    @Test void geral_manager_usesStoreSales() {
        var r = calculator.calculate(hr("M2", 10, 35, 150, LocalDate.of(2013,8,8), null),
            3000.0, 80000.0, 0.010, List.of(), JULY);
        assertThat(r.commissionBase()).isCloseTo(800.0, within(D));
    }

    @Test void admissao_proportionalToDaysWorked() {
        // Admitted July 10: worked = 31-10+1 = 22 days → 10000*0.025*(22/31) = 177.42
        var r = calculator.calculate(hr("M3", 10, 35, 100, LocalDate.of(2025,7,10), null),
            10000.0, 0.0, 0.025, List.of(), JULY);
        assertThat(r.commissionBase()).isCloseTo(177.42, within(D));
        assertThat(r.ruleApplied()).isEqualTo("ADMISSAO");
    }

    @Test void demissao_proportionalToDayOfDismissal() {
        // Dismissed July 10: worked = 10 days → 10000*0.025*(10/31) = 80.65
        var r = calculator.calculate(hr("M4", 10, 35, 100, LocalDate.of(2020,1,1), LocalDate.of(2025,7,10)),
            10000.0, 0.0, 0.025, List.of(), JULY);
        assertThat(r.commissionBase()).isCloseTo(80.65, within(D));
        assertThat(r.ruleApplied()).isEqualTo("DEMISSAO");
    }

    @Test void ferias_deductsVacationDays() {
        // Vacation 10/07-25/07 = 16 days; worked=15 → 10000*0.025*(15/31) = 120.97
        var vacation = MonthlyException.builder().type(ExceptionType.VACATION)
            .matricula("MV").yearMonth("2025-07")
            .startDate(LocalDate.of(2025,7,10)).endDate(LocalDate.of(2025,7,25)).build();
        var r = calculator.calculate(hr("MV", 10, 35, 100, LocalDate.of(2020,1,1), null),
            10000.0, 0.0, 0.025, List.of(vacation), JULY);
        assertThat(r.commissionBase()).isCloseTo(120.97, within(D));
        assertThat(r.ruleApplied()).isEqualTo("FERIAS");
    }

    @Test void afastamentoMenor15_floorApplies() {
        // Absent 5 days (21/07-25/07); worked=26
        // worked_comm=10000*0.025*(26/31)=209.68
        // absence_sales_base=(10000/26)*5=1923.08; absence_comm=48.08 < 3500 → floor
        // base = 209.68 + 3500 = 3709.68
        var absence = MonthlyException.builder().type(ExceptionType.ABSENCE)
            .matricula("MA").yearMonth("2025-07")
            .startDate(LocalDate.of(2025,7,21)).endDate(LocalDate.of(2025,7,25)).build();
        var r = calculator.calculate(hr("MA", 10, 35, 100, LocalDate.of(2020,1,1), null),
            10000.0, 0.0, 0.025, List.of(absence), JULY);
        assertThat(r.commissionBase()).isCloseTo(3709.68, within(D));
        assertThat(r.ruleApplied()).isEqualTo("AFASTAMENTO_MENOR_15");
    }

    @Test void afastamentoMaior15_capsDaysAt15() {
        // Absent 16 days (10/07-25/07); worked=15
        // worked_comm=20000*0.025*(15/31)=241.94
        // capped=15; abs_sales_base=(20000/15)*15=20000; abs_comm=500 < 3500 → floor
        // base = 241.94 + 3500 = 3741.94
        var absence = MonthlyException.builder().type(ExceptionType.ABSENCE)
            .matricula("MB").yearMonth("2025-07")
            .startDate(LocalDate.of(2025,7,10)).endDate(LocalDate.of(2025,7,25)).build();
        var r = calculator.calculate(hr("MB", 10, 35, 100, LocalDate.of(2020,1,1), null),
            20000.0, 0.0, 0.025, List.of(absence), JULY);
        assertThat(r.commissionBase()).isCloseTo(3741.94, within(D));
        assertThat(r.ruleApplied()).isEqualTo("AFASTAMENTO_MAIOR_15");
    }

    @Test void afastamentoFullMonth_returnsFloor() {
        var absence = MonthlyException.builder().type(ExceptionType.ABSENCE)
            .matricula("MC").yearMonth("2025-07")
            .startDate(LocalDate.of(2025,7,1)).endDate(LocalDate.of(2025,7,31)).build();
        var r = calculator.calculate(hr("MC", 10, 35, 100, LocalDate.of(2020,1,1), null),
            0.0, 0.0, 0.025, List.of(absence), JULY);
        assertThat(r.commissionBase()).isCloseTo(3500.0, within(D));
    }

    @Test void rateOverride_absolute_replacesRate() {
        var override = MonthlyException.builder().type(ExceptionType.RATE_OVERRIDE)
            .yearMonth("2025-08").codMarca(10).codCargo(300)
            .overrideRate(0.0175).rateType(RateType.ABSOLUTE).appliesToManagers(false).build();
        var r = calculator.calculate(hr("MR", 10, 35, 300, LocalDate.of(2020,1,1), null),
            10000.0, 0.0, 0.015, List.of(override), YearMonth.of(2025,8));
        assertThat(r.commissionRate()).isCloseTo(0.0175, within(0.0001));
        assertThat(r.commissionBase()).isCloseTo(175.0, within(D));
    }

    @Test void rateOverride_additive_addsToRate() {
        var override = MonthlyException.builder().type(ExceptionType.RATE_OVERRIDE)
            .yearMonth("2025-12").codMarca(40)
            .overrideRate(0.01).rateType(RateType.ADDITIVE).appliesToManagers(false).build();
        var r = calculator.calculate(hr("MD", 40, 10, 100, LocalDate.of(2020,1,1), null),
            10000.0, 0.0, 0.035, List.of(override), YearMonth.of(2025,12));
        assertThat(r.commissionRate()).isCloseTo(0.045, within(0.0001));
        assertThat(r.commissionBase()).isCloseTo(450.0, within(D));
    }

    @Test void rateOverride_additive_skippedForManager() {
        var override = MonthlyException.builder().type(ExceptionType.RATE_OVERRIDE)
            .yearMonth("2025-12").codMarca(40)
            .overrideRate(0.01).rateType(RateType.ADDITIVE).appliesToManagers(false).build();
        var r = calculator.calculate(hr("MG", 40, 10, 150, LocalDate.of(2020,1,1), null),
            5000.0, 80000.0, 0.020, List.of(override), YearMonth.of(2025,12));
        assertThat(r.commissionRate()).isCloseTo(0.020, within(0.0001));
    }

    @Test void bonusFixed_addedToFinalCommission() {
        var bonus = MonthlyException.builder().type(ExceptionType.BONUS_FIXED)
            .matricula("MATRIC-134").yearMonth("2025-08").amount(500.0).build();
        var r = calculator.calculate(hr("MATRIC-134", 10, 35, 100, LocalDate.of(2020,1,1), null),
            10000.0, 0.0, 0.025, List.of(bonus), YearMonth.of(2025,8));
        assertThat(r.finalCommission()).isCloseTo(750.0, within(D)); // 250 + 500
    }

    @Test void salesBonusTier_matchingTierApplied() {
        var tier = MonthlyException.builder().type(ExceptionType.SALES_BONUS_TIER)
            .yearMonth("2025-12").codMarca(10).appliesToManagers(false)
            .bonusTiers(List.of(
                BonusTier.builder().minValue(40000.0).maxValue(50000.0).bonusAmount(3500.0).build(),
                BonusTier.builder().minValue(50000.01).maxValue(60000.0).bonusAmount(4000.0).build(),
                BonusTier.builder().minValue(60000.01).maxValue(null).bonusAmount(4500.0).build()
            )).build();
        var r = calculator.calculate(hr("MS", 10, 35, 100, LocalDate.of(2020,1,1), null),
            45000.0, 0.0, 0.025, List.of(tier), YearMonth.of(2025,12));
        assertThat(r.totalBonuses()).isCloseTo(3500.0, within(D));
    }

    @Test void afastamento_crossMonth_clipsToLastDayOfMonth() {
        // Absence starts Aug 26, ends Sep 5 — for August: effective_end = Aug 31 → absent=6 days (<=15)
        // worked=25; worked_comm=10000*0.025*(25/31)=201.61
        // abs_sales_base=(10000/25)*6=2400; abs_comm=60 < 3500 → floor
        // base = 201.61 + 3500 = 3701.61
        var ym = YearMonth.of(2025, 8); // August: 31 days
        var absence = MonthlyException.builder().type(ExceptionType.ABSENCE)
            .matricula("MATRIC-137").yearMonth("2025-08")
            .startDate(LocalDate.of(2025,8,26)).endDate(LocalDate.of(2025,9,5)).build();
        var r = calculator.calculate(
            HrRecord.builder().matricula("MATRIC-137").codMarca(10).codLoja(5).codCargo(100)
                .descrMarca("M").descrLoja("L").descriCargo("C")
                .dataRef(ym.atDay(1)).dataAdmiss(LocalDate.of(2020,1,1)).dataDemiss(null).build(),
            10000.0, 0.0, 0.025, List.of(absence), ym);
        assertThat(r.ruleApplied()).isEqualTo("AFASTAMENTO_MENOR_15");
        assertThat(r.commissionBase()).isCloseTo(3701.61, within(D));
    }

    @Test void afastamentoExactly15Days_routesToMenor15() {
        // Absent Jul 1–15 = 15 inclusive days (raw diff 14 < 15) → MENOR_15
        // worked=16; worked_comm=10000*0.025*(16/31)=129.03
        // abs_sales_base=(10000/16)*15=9375; abs_comm=234.38 < 3500 → floor
        // base = 129.03 + 3500 = 3629.03
        var absence = MonthlyException.builder().type(ExceptionType.ABSENCE)
            .matricula("M15").yearMonth("2025-07")
            .startDate(LocalDate.of(2025,7,1)).endDate(LocalDate.of(2025,7,15)).build();
        var r = calculator.calculate(hr("M15", 10, 35, 100, LocalDate.of(2020,1,1), null),
            10000.0, 0.0, 0.025, List.of(absence), JULY);
        assertThat(r.ruleApplied()).isEqualTo("AFASTAMENTO_MENOR_15");
        assertThat(r.commissionBase()).isCloseTo(3629.03, within(D));
    }

    @Test void storeBonusTier_appliedToManager() {
        var tier = MonthlyException.builder().type(ExceptionType.STORE_BONUS_TIER)
            .yearMonth("2025-12").appliesToManagers(true)
            .bonusTiers(List.of(
                BonusTier.builder().minValue(120000.0).maxValue(140000.0).bonusAmount(5000.0).build(),
                BonusTier.builder().minValue(140000.01).maxValue(160000.0).bonusAmount(6000.0).build(),
                BonusTier.builder().minValue(160000.01).maxValue(null).bonusAmount(7000.0).build()
            )).build();
        var r = calculator.calculate(hr("MM", 10, 35, 150, LocalDate.of(2020,1,1), null),
            5000.0, 130000.0, 0.010, List.of(tier), YearMonth.of(2025,12));
        assertThat(r.totalBonuses()).isCloseTo(5000.0, within(D));
    }
}
