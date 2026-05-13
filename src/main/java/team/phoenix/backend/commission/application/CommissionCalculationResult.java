package team.phoenix.backend.commission.application;

import java.time.LocalDate;
import java.util.List;
import team.phoenix.backend.commission.domain.CommissionResult;

public record CommissionCalculationResult(
    LocalDate month,
    CommissionTargetType targetType,
    String targetId,
    List<CommissionResult> items,
    double totalCommission,
    List<String> appliedRules
) {
    public static CommissionCalculationResult from(
            LocalDate month,
            CommissionTargetType targetType,
            String targetId,
            List<CommissionResult> items) {
        double total = items.stream()
            .mapToDouble(CommissionResult::finalCommission)
            .sum();
        List<String> rules = items.stream()
            .map(CommissionResult::ruleApplied)
            .distinct()
            .toList();
        return new CommissionCalculationResult(month, targetType, targetId, items, total, rules);
    }
}
