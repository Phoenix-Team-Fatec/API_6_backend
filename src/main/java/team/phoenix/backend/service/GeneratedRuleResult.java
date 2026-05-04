package team.phoenix.backend.service;

import java.util.List;

import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.MonthlyException;

public record GeneratedRuleResult(
    String tipo,
    String justificativa,
    List<CommissionRate> rules,
    List<MonthlyException> exceptions
) {}
