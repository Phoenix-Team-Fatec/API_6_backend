package team.phoenix.backend.audit.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team.phoenix.backend.audit.domain.AuditActorProvider;
import team.phoenix.backend.audit.domain.AuditEvent;
import team.phoenix.backend.audit.domain.AuditLogEntry;
import team.phoenix.backend.audit.domain.AuditLogQuery;
import team.phoenix.backend.audit.domain.AuditLogResult;
import team.phoenix.backend.domain.repository.AuditEventRepository;

// Implementacao que persiste eventos de auditoria no MongoDB e consulta logs com filtros dinamicos.
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditEventRepository auditEventRepository;
    private final MongoTemplate mongoTemplate;
    private final AuditActorProvider auditActorProvider;

    @Override
    public void record(AuditLogEntry entry) {
        // O ator e resolvido por provider para permitir trocar o fallback por autenticacao real depois.
        var actor = auditActorProvider.getCurrentActor();
        var event = AuditEvent.builder()
            .action(entry.action())
            .resourceType(entry.resourceType())
            .resourceId(entry.resourceId())
            .actorEmail(actor.email())
            .actorRole(actor.role())
            .actorSource(actor.source())
            .occurredAt(LocalDateTime.now())
            .summary(entry.summary())
            .metadata(entry.metadata() == null ? Map.of() : entry.metadata())
            .build();

        try {
            auditEventRepository.save(event);
        } catch (RuntimeException ex) {
            // Falha de auditoria nao deve desfazer a operacao principal ja persistida.
            log.warn("Could not persist audit event for {} {}",
                entry.resourceType(), entry.resourceId(), ex);
        }
    }

    @Override
    public List<AuditLogResult> list(AuditLogQuery filter) {
        // MongoTemplate permite montar a busca apenas com os filtros recebidos na requisicao.
        Query query = new Query();
        if (filter.resourceType() != null) {
            query.addCriteria(Criteria.where("resourceType").is(filter.resourceType()));
        }
        if (hasText(filter.resourceId())) {
            query.addCriteria(Criteria.where("resourceId").is(filter.resourceId()));
        }
        if (hasText(filter.actorEmail())) {
            query.addCriteria(Criteria.where("actorEmail").is(filter.actorEmail()));
        }
        if (filter.action() != null) {
            query.addCriteria(Criteria.where("action").is(filter.action()));
        }
        if (filter.from() != null || filter.to() != null) {
            Criteria occurredAt = Criteria.where("occurredAt");
            if (filter.from() != null) {
                occurredAt = occurredAt.gte(filter.from());
            }
            if (filter.to() != null) {
                occurredAt = occurredAt.lte(filter.to());
            }
            query.addCriteria(occurredAt);
        }
        query.with(Sort.by(Sort.Direction.DESC, "occurredAt"));

        return mongoTemplate.find(query, AuditEvent.class).stream()
            .map(AuditLogResult::from)
            .toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
