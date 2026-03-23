package team.phoenix.backend.controller;

import team.phoenix.backend.service.CommissionResult;
import java.util.List;

public record CommissionResponse(
    String matricula,
    String month,
    EmployeeInfo employee,
    double salesBase,
    double commissionRate,
    double commissionBase,
    List<String> bonuses,
    double totalBonuses,
    double finalCommission,
    String ruleApplied,
    String explanation
) {
    public record EmployeeInfo(
        Integer codMarca, String descrMarca,
        Integer codLoja, String descrLoja,
        Integer codCargo, String descriCargo
    ) {}

    public static CommissionResponse from(CommissionResult r) {
        var hr = r.employee();
        return new CommissionResponse(
            r.matricula(), r.month(),
            new EmployeeInfo(hr.getCodMarca(), hr.getDescrMarca(),
                hr.getCodLoja(), hr.getDescrLoja(),
                hr.getCodCargo(), hr.getDescriCargo()),
            r.salesBase(), r.commissionRate(), r.commissionBase(),
            r.bonuses(), r.totalBonuses(), r.finalCommission(),
            r.ruleApplied(), r.explanation()
        );
    }
}
