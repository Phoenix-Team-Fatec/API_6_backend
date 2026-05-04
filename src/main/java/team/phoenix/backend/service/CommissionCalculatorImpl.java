package team.phoenix.backend.service;

import org.springframework.stereotype.Component;
import team.phoenix.backend.domain.model.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// Componente que realiza cálculo complexo de comissões com regras de admissão, demissão, afastamento e férias
@Component
public class CommissionCalculatorImpl implements CommissionCalculator {

    private static final double ABSENCE_FLOOR = 3500.0;
    private static final int MANAGER_CARGO = 150;

    // Calcula comissão para um funcionário em um mês
    // Parâm hr: registro de RH do funcionário
    // Parâm individualSales: total de vendas individuais
    // Parâm storeSales: total de vendas da loja
    // Parâm baseRate: taxa de comissão base
    // Parâm exceptions: excepções do mês (afastamentos, férias, bônus, etc)
    // Parâm month: mês do cálculo
    // Retorna: CommissionResult com cálculo completo e explanação
    @Override
    public CommissionResult calculate(HrRecord hr, double individualSales, double storeSales,
                                      double baseRate, List<MonthlyException> exceptions,
                                      LocalDate month) {
        double effectiveRate = applyRateOverride(baseRate, hr, exceptions);
        double salesBase = hr.getCodCargo() == MANAGER_CARGO ? storeSales : individualSales;
        int daysInMonth = month.lengthOfMonth();
        LocalDate firstDay = month.withDayOfMonth(1);
        LocalDate lastDay = month.withDayOfMonth(daysInMonth);

        var absence = exceptions.stream()
            .filter(e -> e.getType() == ExceptionType.ABSENCE
                && hr.getMatricula().equals(e.getMatricula()))
            .findFirst();
        var vacation = exceptions.stream()
            .filter(e -> e.getType() == ExceptionType.VACATION
                && hr.getMatricula().equals(e.getMatricula()))
            .findFirst();
        var maternityLeave = exceptions.stream()
            .filter(e -> e.getType() == ExceptionType.MATERNITY_LEAVE
                && hr.getMatricula().equals(e.getMatricula()))
            .findFirst();

        // Priority order per spec: ADMISSAO → DEMISSAO → ABSENCE → VACATION → GERAL
        RuleResult rule;
        if (YearMonth.from(hr.getDataAdmiss()).equals(YearMonth.from(month))) {
            int day = hr.getDataAdmiss().getDayOfMonth();
            int worked = daysInMonth - day + 1;
            double c = salesBase * effectiveRate * ((double) worked / daysInMonth);
            rule = new RuleResult(c, "ADMISSAO",
                String.format("Admission day %d: %.2f x %.4f x (%d/%d) = %.2f",
                    day, salesBase, effectiveRate, worked, daysInMonth, c));
        } else if (hr.getDataDemiss() != null && YearMonth.from(hr.getDataDemiss()).equals(YearMonth.from(month))) {
            int d = hr.getDataDemiss().getDayOfMonth();
            double c = salesBase * effectiveRate * ((double) d / daysInMonth);
            rule = new RuleResult(c, "DEMISSAO",
                String.format("Dismissal day %d: %.2f x %.4f x (%d/%d) = %.2f",
                    d, salesBase, effectiveRate, d, daysInMonth, c));
        } else if (absence.isPresent()) {
            rule = applyAbsence(absence.get(), salesBase, effectiveRate, daysInMonth, firstDay, lastDay);
        } else if (vacation.isPresent()) {
            rule = applyVacation(vacation.get(), salesBase, effectiveRate, daysInMonth, firstDay, lastDay);
        } else if (maternityLeave.isPresent()) {
            rule = applyMaternityLeave(maternityLeave.get(), salesBase, effectiveRate, daysInMonth, firstDay, lastDay);
        } else {
            double c = salesBase * effectiveRate;
            rule = new RuleResult(c, "GERAL",
                String.format("%.2f x %.4f = %.2f", salesBase, effectiveRate, c));
        }

        BonusResult bonusResult = applyBonuses(hr, individualSales, storeSales, exceptions);
        return new CommissionResult(hr.getMatricula(), month, hr,
            salesBase, effectiveRate, rule.base(),
            bonusResult.descriptions(), bonusResult.total(),
            rule.base() + bonusResult.total(),
            rule.name(), rule.explanation());
    }

    // Aplica sobrescrita de taxa de comissão baseado em excepções
    // Parâm rate: taxa base
    // Parâm hr: registro de RH
    // Parâm exceptions: lista de excepções
    // Retorna: taxa efetiva após aplicar sobrescritas (ABSOLUTA ou ADITIVA)
    private double applyRateOverride(double rate, HrRecord hr, List<MonthlyException> exceptions) {
        for (var ex : exceptions) {
            if (ex.getType() != ExceptionType.RATE_OVERRIDE) continue;
            if (ex.getCodMarca() != null && !ex.getCodMarca().equals(hr.getCodMarca())) continue;
            if (ex.getCodCargo() != null && !ex.getCodCargo().equals(hr.getCodCargo())) continue;
            if (!ex.isAppliesToManagers() && hr.getCodCargo() == MANAGER_CARGO) continue;
            rate = ex.getRateType() == RateType.ABSOLUTE
                ? ex.getOverrideRate()
                : rate + ex.getOverrideRate();
        }
        return rate;
    }

    // Aplica bônus (fixos e por faixa de venda) ao cálculo
    // Parâm hr: registro de RH
    // Parâm indSales: vendas individuais
    // Parâm storeSales: vendas da loja
    // Parâm exceptions: lista de excepções com bônus
    // Retorna: BonusResult com total e descrição dos bônus
    private BonusResult applyBonuses(HrRecord hr, double indSales, double storeSales,
                                     List<MonthlyException> exceptions) {
        double total = 0.0;
        List<String> descriptions = new ArrayList<>();
        for (var ex : exceptions) {
            switch (ex.getType()) {
                case BONUS_FIXED -> {
                    if (hr.getMatricula().equals(ex.getMatricula())) {
                        total += ex.getAmount();
                        descriptions.add(String.format("BONUS_FIXED: R$%.2f", ex.getAmount()));
                    }
                }
                case SALES_BONUS_TIER -> {
                    if ((ex.getCodMarca() == null || ex.getCodMarca().equals(hr.getCodMarca()))
                            && (ex.isAppliesToManagers() || hr.getCodCargo() != MANAGER_CARGO)) {
                        double bonus = matchTier(ex.getBonusTiers(), indSales);
                        if (bonus > 0) {
                            total += bonus;
                            descriptions.add(String.format("SALES_BONUS_TIER: R$%.2f", bonus));
                        }
                    }
                }
                case STORE_BONUS_TIER -> {
                    if (hr.getCodCargo() == MANAGER_CARGO && ex.isAppliesToManagers()
                            && (ex.getCodMarca() == null || ex.getCodMarca().equals(hr.getCodMarca()))) {
                        double bonus = matchTier(ex.getBonusTiers(), storeSales);
                        if (bonus > 0) {
                            total += bonus;
                            descriptions.add(String.format("STORE_BONUS_TIER: R$%.2f", bonus));
                        }
                    }
                }
                default -> {}
            }
        }
        return new BonusResult(total, descriptions);
    }

    private record BonusResult(double total, List<String> descriptions) {}

    // Aplica desconto de comissão por afastamento
    // Parâm a: excepção de afastamento
    // Parâm salesBase: base de vendas
    // Parâm rate: taxa de comissão
    // Parâm daysInMonth: dias no mês
    // Parâm lastDay: último dia do mês
    // Retorna: RuleResult com comissão após afastamento
    private RuleResult applyAbsence(MonthlyException a, double salesBase, double rate,
                                    int daysInMonth, LocalDate firstDay, LocalDate lastDay) {
        LocalDate start = a.getStartDate().isBefore(firstDay) ? firstDay : a.getStartDate();
        LocalDate end = a.getEndDate().isAfter(lastDay) ? lastDay : a.getEndDate();
        int absent = (int) ChronoUnit.DAYS.between(start, end) + 1;
        int worked = daysInMonth - absent;

        if (worked <= 0) {
            return new RuleResult(ABSENCE_FLOOR, "AFASTAMENTO_MAIOR_15",
                "Full month absent: floor R$3500.00");
        }

        double workedComm = salesBase * rate * ((double) worked / daysInMonth);
        // spec uses raw diff (end - start) < 15; inclusive count = raw_diff + 1, so threshold is absent <= 15
        if (absent <= 15) {
            double absBase = (salesBase / worked) * absent;
            double absComm = absBase * rate;
            double base = workedComm + Math.max(absComm, ABSENCE_FLOOR);
            return new RuleResult(base, "AFASTAMENTO_MENOR_15",
                String.format("Absent %d days (<15): %.2f + max(%.2f, 3500) = %.2f",
                    absent, workedComm, absComm, base));
        } else {
            int capped = Math.min(absent, 15);
            double absBase = (salesBase / worked) * capped;
            double absComm = absBase * rate;
            double base = workedComm + Math.max(absComm, ABSENCE_FLOOR);
            return new RuleResult(base, "AFASTAMENTO_MAIOR_15",
                String.format("Absent %d days (>=15, capped %d): %.2f + max(%.2f, 3500) = %.2f",
                    absent, capped, workedComm, absComm, base));
        }
    }

    // Aplica desconto de comissão por férias
    // Parâm v: excepção de férias
    // Parâm salesBase: base de vendas
    // Parâm rate: taxa de comissão
    // Parâm daysInMonth: dias no mês
    // Parâm lastDay: último dia do mês
    // Retorna: RuleResult com comissão após férias
    private RuleResult applyVacation(MonthlyException v, double salesBase, double rate,
                                     int daysInMonth, LocalDate firstDay, LocalDate lastDay) {
        LocalDate start = v.getStartDate().isBefore(firstDay) ? firstDay : v.getStartDate();
        LocalDate end = v.getEndDate().isAfter(lastDay) ? lastDay : v.getEndDate();
        int vacDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
        int worked = daysInMonth - vacDays;
        double base = salesBase * rate * ((double) worked / daysInMonth);
        return new RuleResult(base, "FERIAS",
            String.format("Vacation %d days: %.2f x %.4f x (%d/%d) = %.2f",
                vacDays, salesBase, rate, worked, daysInMonth, base));
    }

    private RuleResult applyMaternityLeave(MonthlyException m, double salesBase, double rate,
                                           int daysInMonth, LocalDate firstDay, LocalDate lastDay) {
        LocalDate start = m.getStartDate().isBefore(firstDay) ? firstDay : m.getStartDate();
        LocalDate end = m.getEndDate() == null || m.getEndDate().isAfter(lastDay) ? lastDay : m.getEndDate();
        int leaveDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
        int worked = Math.max(0, daysInMonth - leaveDays);
        double base = salesBase * rate * ((double) worked / daysInMonth);
        return new RuleResult(base, "MATERNIDADE",
            String.format("Maternity leave %d days: %.2f x %.4f x (%d/%d) = %.2f",
                leaveDays, salesBase, rate, worked, daysInMonth, base));
    }

    // Encontra o valor de bônus﻿que corresponde à faixa de vendas
    // Parâm tiers: lista de faixas de bônus
    // Parâm value: valor das vendas
    // Retorna: valor do bônus ou 0 se nenhuma faixa corresponder
    private double matchTier(List<BonusTier> tiers, double value) {
        if (tiers == null) return 0.0;
        return tiers.stream()
            .filter(t -> value >= t.getMinValue()
                && (t.getMaxValue() == null || value <= t.getMaxValue()))
            .findFirst()
            .map(BonusTier::getBonusAmount)
            .orElse(0.0);
    }

    private record RuleResult(double base, String name, String explanation) {}
}
