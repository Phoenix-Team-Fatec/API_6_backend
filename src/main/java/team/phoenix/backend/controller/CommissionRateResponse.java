package team.phoenix.backend.controller;

import team.phoenix.backend.domain.model.CommissionRate;

public record CommissionRateResponse(
    String id,
    Integer codMarca,
    String descrMarca,
    Integer codCargo,
    String descriCargo,
    Double pctComiss
) {
    public static CommissionRateResponse from(CommissionRate r) {
        return new CommissionRateResponse(
            r.getId(), r.getCodMarca(), r.getDescrMarca(),
            r.getCodCargo(), r.getDescriCargo(), r.getPctComiss()
        );
    }
}
