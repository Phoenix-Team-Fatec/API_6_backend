package team.phoenix.backend.audit.domain;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditLogResult(
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
    public static AuditLogResult from(AuditEvent event) {
        return new AuditLogResult(
            event.getId(),
            event.getAction(),
            event.getResourceType(),
            event.getResourceId(),
            event.getActorEmail(),
            event.getActorRole(),
            event.getActorSource(),
            event.getOccurredAt(),
            event.getSummary(),
            event.getMetadata()
        );
    }
}
