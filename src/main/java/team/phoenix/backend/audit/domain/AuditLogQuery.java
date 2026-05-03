package team.phoenix.backend.audit.domain;

import java.time.LocalDateTime;

public record AuditLogQuery(
    AuditResourceType resourceType,
    String resourceId,
    String actorEmail,
    AuditAction action,
    LocalDateTime from,
    LocalDateTime to
) {
}
