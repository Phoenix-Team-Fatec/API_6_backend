package team.phoenix.backend.rules.api;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import team.phoenix.backend.rules.application.GeneratedRuleResult;

public record GeneratedRuleResponse(
    String tipo,
    String justificativa,
    List<CommissionRateResponse> rules,
    List<MonthlyExceptionResponse> exceptions,
    @JsonProperty("token_usage") Map<String, Integer> tokenUsage
) {
    public static GeneratedRuleResponse from(GeneratedRuleResult result) {
        return new GeneratedRuleResponse(
            result.tipo(),
            result.justificativa(),
            result.rules().stream().map(CommissionRateResponse::from).toList(),
            result.exceptions().stream().map(MonthlyExceptionResponse::from).toList(),
            result.tokenUsage()
        );
    }
}
