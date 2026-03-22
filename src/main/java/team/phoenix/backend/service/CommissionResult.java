package team.phoenix.backend.service;

import team.phoenix.backend.domain.model.HrRecord;
import java.util.List;

public record CommissionResult(
    String matricula,
    String month,
    HrRecord employee,
    double salesBase,
    double commissionRate,
    double commissionBase,
    List<String> bonuses,
    double totalBonuses,
    double finalCommission,
    String ruleApplied,
    String explanation
) {}
