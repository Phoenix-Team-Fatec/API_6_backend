package team.phoenix.backend.audit.api;

import java.time.LocalDateTime;
import java.util.Map;

import team.phoenix.backend.audit.domain.AuditAction;
import team.phoenix.backend.audit.domain.AuditLogResult;
import team.phoenix.backend.audit.domain.AuditResourceType;

public record AuditLogResponse(
    String id,
    AuditAction action,
    AuditResourceType resourceType,
    String resourceId,
    String actorEmail,
    String actorRole,
    String actorSource,
    LocalDateTime occurredAt,
    String summary,
    Map<String, Object> metadata
) {
    public static AuditLogResponse from(AuditLogResult result) {
        return new AuditLogResponse(
            result.id(),
            result.action(),
            result.resourceType(),
            result.resourceId(),
            result.actorEmail(),
            result.actorRole(),
            result.actorSource(),
            result.occurredAt(),
            result.summary(),
            result.metadata()
        );
    }
}
