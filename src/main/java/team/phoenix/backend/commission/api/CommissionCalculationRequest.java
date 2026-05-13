package team.phoenix.backend.commission.api;

import java.time.LocalDate;
import java.time.YearMonth;

import team.phoenix.backend.commission.application.CommissionCalculationCommand;
import team.phoenix.backend.commission.application.CommissionTargetType;

public record CommissionCalculationRequest(
    String month,
    CommissionTargetType targetType,
    String matricula,
    Integer codLoja,
    Integer codMarca
) {
    public LocalDate getMonthAsLocalDate() {
        if (month == null || month.isBlank()) {
            throw new IllegalArgumentException("month is required");
        }
        return YearMonth.parse(month).atDay(1);
    }

    public CommissionCalculationCommand toCommand() {
        return new CommissionCalculationCommand(month, targetType, matricula, codLoja, codMarca);
    }
}
