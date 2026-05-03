package team.phoenix.backend.audit.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import team.phoenix.backend.audit.domain.AuditAction;
import team.phoenix.backend.audit.domain.AuditActor;
import team.phoenix.backend.audit.domain.AuditActorProvider;
import team.phoenix.backend.audit.domain.AuditEvent;
import team.phoenix.backend.audit.domain.AuditLogEntry;
import team.phoenix.backend.audit.domain.AuditResourceType;
import team.phoenix.backend.domain.repository.AuditEventRepository;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock AuditEventRepository auditEventRepository;
    @Mock AuditActorProvider auditActorProvider;
    @Mock MongoTemplate mongoTemplate;

    @Test
    void record_buildsEventWithActorAndTimestamp() {
        var service = new AuditLogServiceImpl(auditEventRepository, mongoTemplate, auditActorProvider);
        when(auditActorProvider.getCurrentActor()).thenReturn(new AuditActor("admin@example.com", "ADMIN", "HEADER"));
        when(auditEventRepository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.record(new AuditLogEntry(
            AuditAction.UPDATE,
            AuditResourceType.COMMISSION_RATE,
            "rate-1",
            "Regra de comissao atualizada",
            Map.of("codMarca", 10, "codCargo", 100)
        ));

        verify(auditEventRepository).save(argThat(event ->
            event.getAction() == AuditAction.UPDATE
                && event.getResourceType() == AuditResourceType.COMMISSION_RATE
                && event.getResourceId().equals("rate-1")
                && event.getActorEmail().equals("admin@example.com")
                && event.getActorRole().equals("ADMIN")
                && event.getActorSource().equals("HEADER")
                && event.getOccurredAt() != null
                && event.getSummary().equals("Regra de comissao atualizada")
                && event.getMetadata().get("codMarca").equals(10)
        ));
    }

    @Test
    void record_whenRepositoryFails_doesNotPropagateException() {
        var service = new AuditLogServiceImpl(auditEventRepository, mongoTemplate, auditActorProvider);
        when(auditActorProvider.getCurrentActor()).thenReturn(new AuditActor("system", "SYSTEM", "SYSTEM"));
        doThrow(new RuntimeException("mongo unavailable")).when(auditEventRepository).save(any(AuditEvent.class));

        assertThatCode(() -> service.record(new AuditLogEntry(
            AuditAction.UPDATE,
            AuditResourceType.BRAND,
            "brand-1",
            "Marca atualizada",
            Map.of()
        ))).doesNotThrowAnyException();
    }
}
