package team.phoenix.backend.rules.api;

import java.time.LocalDate;
import java.time.YearMonth;

import team.phoenix.backend.domain.model.MonthlyException;
import team.phoenix.backend.domain.model.RateType;

public record CreateScopedRateRuleRequest(
    String yearMonth,
    String startDate,
    String endDate,
    ScopeRequest scope,
    EffectRequest effect
) {
    public MonthlyException toMonthlyException() {
        ScopeRequest resolvedScope = scope == null ? new ScopeRequest(null, null, null, null) : scope;
        EffectRequest resolvedEffect = effect == null ? new EffectRequest(null, null) : effect;

        return MonthlyException.builder()
            .yearMonth(parseYearMonth(yearMonth))
            .startDate(parseDate(startDate))
            .endDate(parseDate(endDate))
            .matricula(resolvedScope.matricula())
            .codLoja(resolvedScope.codLoja())
            .codMarca(resolvedScope.codMarca())
            .codCargo(resolvedScope.codCargo())
            .rateType(resolvedEffect.type())
            .overrideRate(resolvedEffect.rate())
            .build();
    }

    private static LocalDate parseYearMonth(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return YearMonth.parse(value).atDay(1);
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    public record ScopeRequest(String matricula, Integer codLoja, Integer codMarca, Integer codCargo) {}

    public record EffectRequest(RateType type, Double rate) {}
}
