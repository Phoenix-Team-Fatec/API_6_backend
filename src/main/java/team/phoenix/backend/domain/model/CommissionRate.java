package team.phoenix.backend.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Taxa de comissão base por marca e cargo - com versionamento e auditoria
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "commission_rates")
@CompoundIndex(name = "marca_cargo_versao_idx", def = "{'codMarca': 1, 'codCargo': 1, 'versao': 1}", unique = true)
@CompoundIndex(name = "vigente_idx", def = "{'isVigente': 1, 'codMarca': 1, 'codCargo': 1}")
public class CommissionRate {
    @Id private String id;
    private String nomeRegra;
    private Integer codMarca;
    private String descrMarca;
    private Integer codCargo;
    private String descriCargo;
    private Double pctComiss;

    // Novos campos (requisitos #10, #11, #24)
    private LocalDate data;                    // data de vigência (dia 1 do mês)
    private String textoOriginal;             // representação textual (gerador #10)
    private String explicacao;                // pseudocódigo/explicação (gerador #11)

    // Versionamento e auditoria
    private Integer versao;                   // incremental, única por (codMarca, codCargo)
    private Boolean isVigente;                // true = ativa, false = inativa
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;          // null enquanto não editado
    private LocalDateTime deletedAt;          // soft delete

    // Histórico de versões anteriores
    private List<CommissionRateVersion> versoesAnteriores;

    // Construtor legado para compatibilidade
    public CommissionRate(Integer codMarca, String descrMarca, Integer codCargo,
                         String descriCargo, Double pctComiss) {
        this.codMarca = codMarca;
        this.descrMarca = descrMarca;
        this.codCargo = codCargo;
        this.descriCargo = descriCargo;
        this.pctComiss = pctComiss;
        this.versao = 1;
        this.isVigente = true;
        this.createdAt = LocalDateTime.now();
        this.versoesAnteriores = new ArrayList<>();
    }

    // Classe interna para versões anteriores
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CommissionRateVersion {
        private String nomeRegra;
        private Integer codMarca;
        private String descrMarca;
        private Integer codCargo;
        private String descriCargo;
        private Double pctComiss;
        private LocalDate data;
        private Integer versao;
        private String textoOriginal;
        private String explicacao;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean isVigente;
        private LocalDateTime deletedAt;
    }
}
