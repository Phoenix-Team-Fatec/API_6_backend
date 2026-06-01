package team.phoenix.backend.rules.api;

import team.phoenix.backend.domain.model.BonusTier;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;
import team.phoenix.backend.domain.model.RateType;
import java.time.LocalDate;
import java.util.List;

public record MonthlyExceptionResponse(
    String id,
    LocalDate yearMonth,
    ExceptionType type,
    String matricula,
    LocalDate startDate,
    LocalDate endDate,
    Double amount,
    Integer codLoja,
    Integer codMarca,
    Integer codCargo,
    Double overrideRate,
    RateType rateType,
    boolean appliesToManagers,
    Integer alternateCodLoja,
    Integer daysWorked,
    List<BonusTier> bonusTiers
) {
    public static MonthlyExceptionResponse from(MonthlyException e) {
        return new MonthlyExceptionResponse(
            e.getId(), e.getYearMonth(), e.getType(), e.getMatricula(),
            e.getStartDate(), e.getEndDate(), e.getAmount(),
            e.getCodLoja(), e.getCodMarca(), e.getCodCargo(), e.getOverrideRate(),
            e.getRateType(), e.isAppliesToManagers(),
            e.getAlternateCodLoja(), e.getDaysWorked(), e.getBonusTiers()
        );
    }
}
