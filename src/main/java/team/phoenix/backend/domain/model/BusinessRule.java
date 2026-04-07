package team.phoenix.backend.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

// Regra de negócio criada a partir de texto em linguagem natural e interpretada pela IA
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "business_rules")
public class BusinessRule {
    @Id private String id;
    private String rawText;
    private String changeSummary;
    private String diff;
    private String targetFile;
    private String updatedCode;
    private int version;
    private LocalDateTime createdAt;
}
