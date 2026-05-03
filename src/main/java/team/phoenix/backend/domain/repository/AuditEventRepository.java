package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import team.phoenix.backend.audit.domain.AuditEvent;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
}
