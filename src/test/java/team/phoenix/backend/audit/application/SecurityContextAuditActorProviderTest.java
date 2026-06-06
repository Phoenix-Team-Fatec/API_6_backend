package team.phoenix.backend.audit.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityContextAuditActorProviderTest {

    private final SecurityContextAuditActorProvider provider = new SecurityContextAuditActorProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentActor_withoutAuthentication_returnsSystemActor() {
        var actor = provider.getCurrentActor();

        assertThat(actor.email()).isEqualTo("system");
        assertThat(actor.role()).isEqualTo("SYSTEM");
        assertThat(actor.source()).isEqualTo("SYSTEM");
    }

    @Test
    void getCurrentActor_withAdminUser_returnsJwtActorWithAdminRole() {
        setAuthentication("admin@example.com", "ROLE_ADMIN");

        var actor = provider.getCurrentActor();

        assertThat(actor.email()).isEqualTo("admin@example.com");
        assertThat(actor.role()).isEqualTo("ADMIN");
        assertThat(actor.source()).isEqualTo("JWT");
    }

    @Test
    void getCurrentActor_withRegularUser_returnsJwtActorWithUserRole() {
        setAuthentication("user@example.com", "ROLE_USER");

        var actor = provider.getCurrentActor();

        assertThat(actor.email()).isEqualTo("user@example.com");
        assertThat(actor.role()).isEqualTo("USER");
        assertThat(actor.source()).isEqualTo("JWT");
    }

    @Test
    void getCurrentActor_withAnonymousAuthentication_returnsSystemActor() {
        var anon = new AnonymousAuthenticationToken(
            "key", "anonymousUser",
            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anon);

        var actor = provider.getCurrentActor();

        assertThat(actor.email()).isEqualTo("system");
        assertThat(actor.role()).isEqualTo("SYSTEM");
        assertThat(actor.source()).isEqualTo("SYSTEM");
    }

    private void setAuthentication(String email, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
            email, null,
            List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
