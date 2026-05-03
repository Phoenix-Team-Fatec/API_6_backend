package team.phoenix.backend.audit.domain;

public interface AuditActorProvider {
    AuditActor getCurrentActor();
}
