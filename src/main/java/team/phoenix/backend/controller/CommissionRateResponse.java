package team.phoenix.backend.controller;

import team.phoenix.backend.domain.model.CommissionRate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CommissionRateResponse(
    String id,
    Integer codMarca,
    String descrMarca,
    Integer codCargo,
    String descriCargo,
    Double pctComiss,
    LocalDate data,                        // dia 1 do mês
    String textoOriginal,                 // novo
    String explicacao,                    // novo
    Integer versao,                       // novo
    Boolean isVigente,                    // novo
    LocalDateTime createdAt,              // novo
    LocalDateTime updatedAt,              // novo
    LocalDateTime deletedAt,              // novo
    List<CommissionRateVersionResponse> versoesAnteriores  // novo
) {
    public static CommissionRateResponse from(CommissionRate r) {
        return new CommissionRateResponse(
            r.getId(),
            r.getCodMarca(),
            r.getDescrMarca(),
            r.getCodCargo(),
            r.getDescriCargo(),
            r.getPctComiss(),
            r.getData(),
            r.getTextoOriginal(),
            r.getExplicacao(),
            r.getVersao(),
            r.getIsVigente(),
            r.getCreatedAt(),
            r.getUpdatedAt(),
            r.getDeletedAt(),
            r.getVersoesAnteriores() != null ?
                r.getVersoesAnteriores().stream()
                    .map(CommissionRateVersionResponse::from)
                    .toList() : List.of()
        );
    }

    // DTO para versões anteriores
    public record CommissionRateVersionResponse(
        Integer versao,
        Double pctComiss,
        String textoOriginal,
        String explicacao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean isVigente
    ) {
        public static CommissionRateVersionResponse from(CommissionRate.CommissionRateVersion v) {
            return new CommissionRateVersionResponse(
                v.getVersao(),
                v.getPctComiss(),
                v.getTextoOriginal(),
                v.getExplicacao(),
                v.getCreatedAt(),
                v.getUpdatedAt(),
                v.getIsVigente()
            );
        }
    }
}
