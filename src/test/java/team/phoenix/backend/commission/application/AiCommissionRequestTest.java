package team.phoenix.backend.commission.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;
import team.phoenix.backend.domain.model.RateType;

class AiCommissionRequestTest {

    private static final LocalDate JULY = LocalDate.of(2025, 7, 1);

    @Test
    void mapsScopedRateOverridesWithScopeAndEffect() {
        var storeAbsolute = MonthlyException.builder()
            .yearMonth(JULY)
            .type(ExceptionType.RATE_OVERRIDE)
            .codLoja(75)
            .overrideRate(0.06)
            .rateType(RateType.ABSOLUTE)
            .build();
        var employeeAdditional = MonthlyException.builder()
            .yearMonth(JULY)
            .type(ExceptionType.RATE_OVERRIDE)
            .matricula("MATRIC-123")
            .overrideRate(0.01)
            .rateType(RateType.ADDITIVE)
            .startDate(LocalDate.of(2025, 7, 10))
            .endDate(LocalDate.of(2025, 7, 20))
            .build();

        var request = AiCommissionRequest.from(
            List.of(),
            List.of(),
            List.of(),
            List.of(storeAbsolute, employeeAdditional),
            List.of(),
            JULY
        );

        assertThat(request.regrasMongo()).hasSize(1);
        var document = request.regrasMongo().get(0);
        assertThat(document).containsEntry("tipo", "rate_override");

        @SuppressWarnings("unchecked")
        var overrides = (List<Map<String, Object>>) document.get("rate_overrides");
        assertThat(overrides).hasSize(2);

        @SuppressWarnings("unchecked")
        var storeScope = (Map<String, Object>) overrides.get(0).get("escopo");
        @SuppressWarnings("unchecked")
        var storeEffect = (Map<String, Object>) overrides.get(0).get("efeito");
        assertThat(storeScope).containsEntry("cod_loja", 75);
        assertThat(storeScope).containsEntry("matricula", null);
        assertThat(storeEffect).containsEntry("tipo", "percentual_absoluto");
        assertThat(storeEffect).containsEntry("valor", 0.06);
        assertThat(overrides.get(0)).containsEntry("vigencia_inicio", JULY);
        assertThat(overrides.get(0)).containsEntry("vigencia_fim", LocalDate.of(2025, 7, 31));

        @SuppressWarnings("unchecked")
        var employeeScope = (Map<String, Object>) overrides.get(1).get("escopo");
        @SuppressWarnings("unchecked")
        var employeeEffect = (Map<String, Object>) overrides.get(1).get("efeito");
        assertThat(employeeScope).containsEntry("matricula", "MATRIC-123");
        assertThat(employeeEffect).containsEntry("tipo", "percentual_adicional");
        assertThat(employeeEffect).containsEntry("valor", 0.01);
        assertThat(overrides.get(1)).containsEntry("vigencia_inicio", LocalDate.of(2025, 7, 10));
        assertThat(overrides.get(1)).containsEntry("vigencia_fim", LocalDate.of(2025, 7, 20));
    }

    @Test
    void ignoresUnscopedLegacyRateOverridesInMlPayload() {
        var legacy = MonthlyException.builder()
            .yearMonth(JULY)
            .type(ExceptionType.RATE_OVERRIDE)
            .overrideRate(0.02)
            .rateType(RateType.ADDITIVE)
            .build();

        var request = AiCommissionRequest.from(
            List.of(),
            List.of(),
            List.of(),
            List.of(legacy),
            List.of(),
            JULY
        );

        assertThat(request.regrasMongo()).isEmpty();
    }
}
