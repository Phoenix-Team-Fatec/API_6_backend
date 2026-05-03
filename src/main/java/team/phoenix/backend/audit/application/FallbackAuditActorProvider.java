package team.phoenix.backend.audit.application;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import team.phoenix.backend.audit.domain.AuditActor;
import team.phoenix.backend.audit.domain.AuditActorProvider;

// Provider temporario de ator enquanto a autenticacao ainda nao esta integrada nesta branch.
@Component
public class FallbackAuditActorProvider implements AuditActorProvider {

    private static final String ACTOR_EMAIL_HEADER = "X-Actor-Email";
    private static final String ACTOR_ROLE_HEADER = "X-Actor-Role";

    @Override
    public AuditActor getCurrentActor() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return systemActor();
        }

        String email = request.getHeader(ACTOR_EMAIL_HEADER);
        String role = request.getHeader(ACTOR_ROLE_HEADER);
        // Headers opcionais permitem testar auditoria antes de existir usuario autenticado.
        boolean hasActorHeader = StringUtils.hasText(email) || StringUtils.hasText(role);
        if (!hasActorHeader) {
            return systemActor();
        }

        return new AuditActor(
            StringUtils.hasText(email) ? email.trim() : "system",
            StringUtils.hasText(role) ? role.trim() : "SYSTEM",
            "HEADER"
        );
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }

    private AuditActor systemActor() {
        return new AuditActor("system", "SYSTEM", "SYSTEM");
    }
}
