package team.phoenix.backend.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "hr_records")
@CompoundIndex(name = "matricula_ref_idx", def = "{'matricula': 1, 'dataRef': 1}")
public class HrRecord {
    @Id private String id;
    private LocalDate dataRef;
    private Integer codMarca;
    private String descrMarca;
    private Integer codLoja;
    private String descrLoja;
    private String matricula;
    private LocalDate dataAdmiss;
    private LocalDate dataDemiss;
    private Integer codCargo;
    private String descriCargo;
}
