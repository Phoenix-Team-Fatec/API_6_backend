package team.phoenix.backend.controller;

import java.util.List;

import team.phoenix.backend.service.GeneratedRuleResult;

public record GeneratedRuleResponse(
    String tipo,
    String justificativa,
    List<CommissionRateResponse> rules,
    List<MonthlyExceptionResponse> exceptions
) {
    public static GeneratedRuleResponse from(GeneratedRuleResult result) {
        return new GeneratedRuleResponse(
            result.tipo(),
            result.justificativa(),
            result.rules().stream().map(CommissionRateResponse::from).toList(),
            result.exceptions().stream().map(MonthlyExceptionResponse::from).toList()
        );
    }
}
