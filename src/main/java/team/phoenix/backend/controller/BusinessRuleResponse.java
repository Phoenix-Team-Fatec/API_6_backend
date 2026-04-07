package team.phoenix.backend.controller;

import team.phoenix.backend.domain.model.BusinessRule;
import java.time.LocalDateTime;

// DTO de saída com os dados da regra de negócio interpretada e persistida
public record BusinessRuleResponse(
    String id,
    String rawText,
    String changeSummary,
    String diff,
    String targetFile,
    int version,
    LocalDateTime createdAt
) {
    public static BusinessRuleResponse from(BusinessRule r) {
        return new BusinessRuleResponse(
            r.getId(), r.getRawText(), r.getChangeSummary(),
            r.getDiff(), r.getTargetFile(), r.getVersion(), r.getCreatedAt()
        );
    }
}
