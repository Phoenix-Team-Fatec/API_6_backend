package team.phoenix.backend.rules.application;

import java.util.List;
import java.util.Map;

import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.MonthlyException;

public record GeneratedRuleResult(
    String tipo,
    String justificativa,
    List<CommissionRate> rules,
    List<MonthlyException> exceptions,
    Map<String, Integer> tokenUsage
) {}
