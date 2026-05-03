package team.phoenix.backend.audit.domain;

// Representa o responsavel pela acao auditada e a origem usada para identifica-lo.
public record AuditActor(
    String email,
    String role,
    String source
) {
}
