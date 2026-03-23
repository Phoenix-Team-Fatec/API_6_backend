package team.phoenix.backend.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "sales_records")
@CompoundIndexes({
    @CompoundIndex(name = "matricula_ref_idx", def = "{'matricula': 1, 'dateRef': 1}"),
    @CompoundIndex(name = "loja_ref_idx", def = "{'codLoja': 1, 'dateRef': 1}")
})
public class SalesRecord {
    @Id private String id;
    private LocalDate dateRef;
    private Integer codMarca;
    private String descrMarca;
    private Integer codLoja;
    private String descrLoja;
    private String matricula;
    private Double vlrVenda;
}
