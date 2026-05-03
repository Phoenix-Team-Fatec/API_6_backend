package team.phoenix.backend.audit.application;

import java.util.List;

import team.phoenix.backend.audit.domain.AuditLogEntry;
import team.phoenix.backend.audit.domain.AuditLogQuery;
import team.phoenix.backend.audit.domain.AuditLogResult;

public interface AuditLogService {
    void record(AuditLogEntry entry);
    List<AuditLogResult> list(AuditLogQuery query);
}
