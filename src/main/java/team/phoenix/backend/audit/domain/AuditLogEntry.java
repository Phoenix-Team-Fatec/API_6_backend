package team.phoenix.backend.audit.domain;

import java.util.Map;

// Entrada usada pelos services para registrar uma alteracao sem depender do documento Mongo.
public record AuditLogEntry(
    AuditAction action,
    AuditResourceType resourceType,
    String resourceId,
    String summary,
    Map<String, Object> metadata
) {
}
