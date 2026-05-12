package team.phoenix.backend.audit.domain;

import java.time.LocalDateTime;

// Filtros opcionais aceitos pela consulta de logs de auditoria.
public record AuditLogQuery(
    AuditResourceType resourceType,
    String resourceId,
    String actorEmail,
    AuditAction action,
    LocalDateTime from,
    LocalDateTime to
) {
}
