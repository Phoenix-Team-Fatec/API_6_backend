package team.phoenix.backend.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "monthly_exceptions")
public class MonthlyException {
    @Id private String id;
    @Indexed private String yearMonth;
    private String matricula;
    private ExceptionType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double amount;
    private Integer codMarca;
    private Integer codCargo;
    private Double overrideRate;
    private RateType rateType;
    private List<BonusTier> bonusTiers;
    private boolean appliesToManagers;
}
