package team.phoenix.backend.audit.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import team.phoenix.backend.audit.application.AuditLogService;
import team.phoenix.backend.audit.domain.AuditAction;
import team.phoenix.backend.audit.domain.AuditLogQuery;
import team.phoenix.backend.audit.domain.AuditLogResult;
import team.phoenix.backend.audit.domain.AuditResourceType;

@WebMvcTest(AuditLogController.class)
class AuditLogControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AuditLogService auditLogService;

    @Test
    void listAuditLogs_withFilters_returnsMappedLogs() throws Exception {
        var result = new AuditLogResult(
            "audit-1",
            AuditAction.UPDATE,
            AuditResourceType.COMMISSION_RATE,
            "rate-1",
            "admin@example.com",
            "ADMIN",
            "HEADER",
            LocalDateTime.of(2026, 5, 2, 10, 0),
            "Regra de comissao atualizada",
            Map.of("codMarca", 10)
        );
        when(auditLogService.list(any(AuditLogQuery.class))).thenReturn(List.of(result));

        mockMvc.perform(get("/api/audit-logs")
                .param("resourceType", "COMMISSION_RATE")
                .param("resourceId", "rate-1")
                .param("actorEmail", "admin@example.com")
                .param("action", "UPDATE")
                .param("from", "2026-05-01T00:00:00")
                .param("to", "2026-05-03T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("audit-1"))
            .andExpect(jsonPath("$[0].actorEmail").value("admin@example.com"))
            .andExpect(jsonPath("$[0].metadata.codMarca").value(10));
    }
}
