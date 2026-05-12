package team.phoenix.backend.audit.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.audit.application.AuditLogService;
import team.phoenix.backend.audit.domain.AuditAction;
import team.phoenix.backend.audit.domain.AuditLogQuery;
import team.phoenix.backend.audit.domain.AuditResourceType;

// Controlador REST para consulta dos eventos de auditoria registrados no sistema.
@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Lista logs de auditoria aplicando filtros opcionais por recurso, ator, acao e periodo.
     */
    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> listAuditLogs(
            @RequestParam(required = false) AuditResourceType resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        var query = new AuditLogQuery(resourceType, resourceId, actorEmail, action, from, to);
        var response = auditLogService.list(query).stream()
            .map(AuditLogResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }
}
