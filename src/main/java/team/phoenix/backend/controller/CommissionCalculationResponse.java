package team.phoenix.backend.controller;

import java.time.LocalDate;
import java.util.List;

import team.phoenix.backend.service.CommissionCalculationResult;
import team.phoenix.backend.service.CommissionTargetType;

public record CommissionCalculationResponse(
    LocalDate month,
    CommissionTargetType targetType,
    String targetId,
    List<CommissionResponse> items,
    double totalCommission,
    List<String> appliedRules
) {
    public static CommissionCalculationResponse from(CommissionCalculationResult result) {
        List<CommissionResponse> items = result.items().stream()
            .map(CommissionResponse::from)
            .toList();
        return new CommissionCalculationResponse(
            result.month(),
            result.targetType(),
            result.targetId(),
            items,
            result.totalCommission(),
            result.appliedRules()
        );
    }
}
