package team.phoenix.backend.audit.domain;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_events")
public class AuditEvent {
    @Id private String id;
    @Indexed private AuditAction action;
    @Indexed private AuditResourceType resourceType;
    @Indexed private String resourceId;
    @Indexed private String actorEmail;
    private String actorRole;
    private String actorSource;
    @Indexed private LocalDateTime occurredAt;
    private String summary;
    private Map<String, Object> metadata;
}
