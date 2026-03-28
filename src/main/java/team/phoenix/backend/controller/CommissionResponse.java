package team.phoenix.backend.controller;

import team.phoenix.backend.service.CommissionResult;
import java.util.List;

// DTO que formata resultado de cálculo de comissão para resposta HTTP
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

    // Converte CommissionResult em CommissionResponse
    // Parâm r: resultado do cálculo de comissão
    // Retorna: DTO formatado para resposta HTTP
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
