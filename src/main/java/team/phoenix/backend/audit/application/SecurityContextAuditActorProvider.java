package team.phoenix.backend.audit.application;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import team.phoenix.backend.audit.domain.AuditActor;
import team.phoenix.backend.audit.domain.AuditActorProvider;

@Component
public class SecurityContextAuditActorProvider implements AuditActorProvider {

    @Override
    public AuditActor getCurrentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(auth)) {
            return systemActor();
        }
        String email = auth.getName();
        String role = auth.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
            .orElse("SYSTEM");
        return new AuditActor(email, role, "JWT");
    }

    private boolean isAuthenticated(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
            .noneMatch(a -> "ROLE_ANONYMOUS".equals(a.getAuthority()));
    }

    private AuditActor systemActor() {
        return new AuditActor("system", "SYSTEM", "SYSTEM");
    }
}
