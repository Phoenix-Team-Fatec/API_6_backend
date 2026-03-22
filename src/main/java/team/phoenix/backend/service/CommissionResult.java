package team.phoenix.backend.service;

import team.phoenix.backend.domain.model.HrRecord;

public record CommissionResult(
    String matricula,
    String month,
    HrRecord employee,
    double salesBase,
    double commissionRate,
    double commissionBase,
    double totalBonuses,
    double finalCommission,
    String ruleApplied,
    String explanation
) {}
