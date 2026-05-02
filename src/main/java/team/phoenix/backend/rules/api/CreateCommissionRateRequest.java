package team.phoenix.backend.rules.api;

import java.time.LocalDate;
import java.time.YearMonth;

// DTO para criar/editar regra de comissão
public record CreateCommissionRateRequest(
    Integer codMarca,
    String descrMarca,
    Integer codCargo,
    String descriCargo,
    Double pctComiss,
    String data,                          // opcional, formato yyyy-MM; se null, usa hoje
    String textoOriginal,                 // opcional; se null/em branco, backend gera
    String explicacao                     // opcional; se null/em branco, backend gera
) {
    public LocalDate getDataAsLocalDate() {
        if (data == null || data.isBlank()) {
            return YearMonth.now().atDay(1);
        }
        return YearMonth.parse(data).atDay(1);
    }

    public LocalDate getDataAsLocalDateOrNull() {
        if (data == null || data.isBlank()) {
            return null;
        }
        return YearMonth.parse(data).atDay(1);
    }
}
