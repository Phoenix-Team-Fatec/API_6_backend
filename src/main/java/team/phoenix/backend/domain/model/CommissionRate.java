package team.phoenix.backend.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

// Taxa de comissão base por marca e cargo
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "commission_rates")
@CompoundIndex(name = "marca_cargo_idx", def = "{'codMarca': 1, 'codCargo': 1}", unique = true)
public class CommissionRate {
    @Id private String id;
    private Integer codMarca;
    private String descrMarca;
    private Integer codCargo;
    private String descriCargo;
    private Double pctComiss;
}
