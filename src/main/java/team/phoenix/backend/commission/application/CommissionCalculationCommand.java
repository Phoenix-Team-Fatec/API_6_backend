package team.phoenix.backend.commission.application;

import java.time.LocalDate;
import java.time.YearMonth;

public record CommissionCalculationCommand(
    String month,
    CommissionTargetType targetType,
    String matricula,
    Integer codLoja,
    Integer codMarca
) {
    public LocalDate monthAsLocalDate() {
        if (month == null || month.isBlank()) {
            throw new IllegalArgumentException("month is required");
        }
        return YearMonth.parse(month).atDay(1);
    }
}
