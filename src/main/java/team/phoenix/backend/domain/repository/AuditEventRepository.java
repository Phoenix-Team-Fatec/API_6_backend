package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import team.phoenix.backend.audit.domain.AuditEvent;

// Repositorio MongoDB para persistir eventos de auditoria.
public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
}
