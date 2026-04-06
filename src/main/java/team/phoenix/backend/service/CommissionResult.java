package team.phoenix.backend.service;

import team.phoenix.backend.domain.model.HrRecord;
import java.time.LocalDate;
import java.util.List;

// DTO que encapsula resultado completo do cálculo de comissão com todos os detalhes
public record CommissionResult(
    String matricula,
    LocalDate month,
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
