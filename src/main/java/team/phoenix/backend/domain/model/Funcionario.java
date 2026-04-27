package team.phoenix.backend.domain.model;

import java.time.LocalDate;
import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "funcionarios")
@CompoundIndex(name = "matricula_ativo_idx", def = "{'matricula': 1, 'ativo': 1}")
public class Funcionario {
    @Id
    private ObjectId id;

    // Dados consolidados de hr_records
    private String matricula;
    private LocalDate dataRef;        // Data de referência mais recente
    private Integer codMarca;
    private String descrMarca;
    private Integer codLoja;
    private String descrLoja;
    private LocalDate dataAdmiss;
    private LocalDate dataDemiss;
    private Integer codCargo;
    private String descriCargo;

    // Dados originais de funcionarios (compatibilidade)
    private Date yearMonth;
    private String type;
    private Date startDate;
    private Date endDate;
    private boolean appliesToManagers;
    private Cargo cargo;
    private CommissionRate comissao;

    // Auditoria e controle
    @Builder.Default
    private boolean ativo = true;     // Soft delete: true = ativo, false = deletado
    private Date criadoEm;
    private Date atualizadoEm;
    private Date deletedAt;           // Para compatibilidade com código antigo
}