package team.phoenix.backend.audit.domain;

import java.util.Map;

public record AuditLogEntry(
    AuditAction action,
    AuditResourceType resourceType,
    String resourceId,
    String summary,
    Map<String, Object> metadata
) {
}
