package team.phoenix.backend.domain.model;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
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
public class Funcionario {
    @Id
    private ObjectId id;

    private Date yearMonth;
    private String matricula;
    private String type;
    private Date startDate;
    private Date endDate;
    private boolean appliesToManagers;
    private Cargo cargo;
    private CommissionRate comissao;
    private Date deletedAt;
}