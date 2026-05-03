package team.phoenix.backend.audit.domain;

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
