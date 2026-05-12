package team.phoenix.backend.audit.application;

import java.util.List;

import team.phoenix.backend.audit.domain.AuditLogEntry;
import team.phoenix.backend.audit.domain.AuditLogQuery;
import team.phoenix.backend.audit.domain.AuditLogResult;

// Interface que centraliza a gravacao e consulta dos logs de auditoria.
public interface AuditLogService {
    /**
     * Registra uma alteracao relevante feita em entidade de negocio.
     */
    void record(AuditLogEntry entry);

    /**
     * Lista eventos de auditoria de acordo com os filtros informados.
     */
    List<AuditLogResult> list(AuditLogQuery query);
}
