package team.phoenix.backend.commission.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.HrRecord;
import team.phoenix.backend.domain.model.MonthlyException;
import team.phoenix.backend.domain.model.RateType;

public class RateOverrideResolver {

    private static final int MANAGER_CARGO = 150;

    public double resolve(double baseRate, HrRecord employee, List<MonthlyException> exceptions) {
        if (employee == null || exceptions == null || exceptions.isEmpty()) {
            return baseRate;
        }

        var applicable = exceptions.stream()
            .filter(exception -> exception.getType() == ExceptionType.RATE_OVERRIDE)
            .filter(exception -> exception.getOverrideRate() != null)
            .filter(exception -> exception.getRateType() != null)
            .filter(exception -> matches(employee, exception))
            .toList();

        double selectedRate = applicable.stream()
            .filter(exception -> exception.getRateType() == RateType.ABSOLUTE)
            .max(Comparator.comparingInt(this::specificity))
            .map(MonthlyException::getOverrideRate)
            .orElse(baseRate);

        double additiveRate = applicable.stream()
            .filter(exception -> exception.getRateType() == RateType.ADDITIVE)
            .map(MonthlyException::getOverrideRate)
            .mapToDouble(Double::doubleValue)
            .sum();

        return selectedRate + additiveRate;
    }

    private boolean matches(HrRecord employee, MonthlyException exception) {
        if (!matchesNullable(exception.getMatricula(), employee.getMatricula())) {
            return false;
        }
        if (!matchesNullable(exception.getCodLoja(), employee.getCodLoja())) {
            return false;
        }
        if (!matchesNullable(exception.getCodMarca(), employee.getCodMarca())) {
            return false;
        }
        if (!matchesNullable(exception.getCodCargo(), employee.getCodCargo())) {
            return false;
        }

        boolean broadLegacyRule = exception.getMatricula() == null
            && exception.getCodLoja() == null
            && exception.getCodCargo() == null;
        if (broadLegacyRule && !exception.isAppliesToManagers()
                && Objects.equals(employee.getCodCargo(), MANAGER_CARGO)) {
            return false;
        }

        return true;
    }

    private boolean matchesNullable(Object expected, Object actual) {
        return expected == null || Objects.equals(expected, actual);
    }

    private int specificity(MonthlyException exception) {
        int score = 0;
        if (exception.getCodLoja() != null) {
            score++;
        }
        if (exception.getCodMarca() != null) {
            score++;
        }
        if (exception.getCodCargo() != null) {
            score++;
        }
        if (exception.getMatricula() != null) {
            score += 100;
        }
        return score;
    }
}
