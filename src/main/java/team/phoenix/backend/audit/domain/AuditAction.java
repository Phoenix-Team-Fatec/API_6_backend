package team.phoenix.backend.audit.domain;

// Acoes de negocio que podem ser registradas no historico de auditoria.
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    SOFT_DELETE,
    ACTIVATE,
    DEACTIVATE,
    RESTORE,
    ROLLBACK,
    CONSOLIDATE,
    IMPORT
}
