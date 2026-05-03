package team.phoenix.backend.audit.domain;

public record AuditActor(
    String email,
    String role,
    String source
) {
}
