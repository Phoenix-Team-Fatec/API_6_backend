package team.phoenix.backend.commission.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.HrRecord;
import team.phoenix.backend.domain.model.MonthlyException;
import team.phoenix.backend.domain.model.RateType;

class RateOverrideResolverTest {

    private static final LocalDate JULY = LocalDate.of(2025, 7, 1);

    private final RateOverrideResolver resolver = new RateOverrideResolver();

    @Test
    void storeAbsoluteAppliesOnlyWhenStoreMatches() {
        var employee = hr("M1", 10, 75, 100);
        var otherStore = hr("M2", 10, 35, 100);
        var storeOverride = rateOverride().codLoja(75)
            .overrideRate(0.06).rateType(RateType.ABSOLUTE).build();

        assertThat(resolver.resolve(0.025, employee, List.of(storeOverride)))
            .isCloseTo(0.06, within(0.0001));
        assertThat(resolver.resolve(0.025, otherStore, List.of(storeOverride)))
            .isCloseTo(0.025, within(0.0001));
    }

    @Test
    void storeAndRoleScopeRequiresBothFieldsToMatch() {
        var counterSeller = hr("M1", 10, 75, 200);
        var storeSeller = hr("M2", 10, 75, 100);
        var scopedOverride = rateOverride().codLoja(75).codCargo(200)
            .overrideRate(0.04).rateType(RateType.ABSOLUTE).build();

        assertThat(resolver.resolve(0.025, counterSeller, List.of(scopedOverride)))
            .isCloseTo(0.04, within(0.0001));
        assertThat(resolver.resolve(0.025, storeSeller, List.of(scopedOverride)))
            .isCloseTo(0.025, within(0.0001));
    }

    @Test
    void employeeAbsoluteIsMoreSpecificThanStoreAbsolute() {
        var employee = hr("MATRIC-123", 10, 75, 100);
        var storeOverride = rateOverride().codLoja(75)
            .overrideRate(0.06).rateType(RateType.ABSOLUTE).build();
        var employeeOverride = rateOverride().matricula("MATRIC-123")
            .overrideRate(0.03).rateType(RateType.ABSOLUTE).build();

        assertThat(resolver.resolve(0.025, employee, List.of(storeOverride, employeeOverride)))
            .isCloseTo(0.03, within(0.0001));
    }

    @Test
    void additiveOverridesAreSummedAfterMostSpecificAbsolute() {
        var employee = hr("MATRIC-123", 10, 75, 100);
        var storeAbsolute = rateOverride().codLoja(75)
            .overrideRate(0.06).rateType(RateType.ABSOLUTE).build();
        var employeeAdditional = rateOverride().matricula("MATRIC-123")
            .overrideRate(0.01).rateType(RateType.ADDITIVE).build();
        var roleAdditional = rateOverride().codCargo(100)
            .overrideRate(0.005).rateType(RateType.ADDITIVE).build();

        assertThat(resolver.resolve(0.025, employee, List.of(storeAbsolute, employeeAdditional, roleAdditional)))
            .isCloseTo(0.075, within(0.0001));
    }

    @Test
    void broadOverrideWithoutManagerFlagDoesNotApplyToManager() {
        var manager = hr("M1", 10, 75, 150);
        var brandOverride = rateOverride().codMarca(10)
            .overrideRate(0.01).rateType(RateType.ADDITIVE).appliesToManagers(false).build();
        var managerOverride = rateOverride().codCargo(150)
            .overrideRate(0.05).rateType(RateType.ABSOLUTE).appliesToManagers(false).build();

        assertThat(resolver.resolve(0.02, manager, List.of(brandOverride)))
            .isCloseTo(0.02, within(0.0001));
        assertThat(resolver.resolve(0.02, manager, List.of(managerOverride)))
            .isCloseTo(0.05, within(0.0001));
    }

    private static MonthlyException.MonthlyExceptionBuilder rateOverride() {
        return MonthlyException.builder()
            .yearMonth(JULY)
            .type(ExceptionType.RATE_OVERRIDE);
    }

    private static HrRecord hr(String matricula, int codMarca, int codLoja, int codCargo) {
        return HrRecord.builder()
            .matricula(matricula)
            .codMarca(codMarca)
            .codLoja(codLoja)
            .codCargo(codCargo)
            .dataRef(JULY)
            .dataAdmiss(LocalDate.of(2020, 1, 1))
            .build();
    }
}
