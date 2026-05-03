package team.phoenix.backend.audit.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class FallbackAuditActorProviderTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getCurrentActor_withoutRequest_returnsSystemActor() {
        var provider = new FallbackAuditActorProvider();

        var actor = provider.getCurrentActor();

        assertThat(actor.email()).isEqualTo("system");
        assertThat(actor.role()).isEqualTo("SYSTEM");
        assertThat(actor.source()).isEqualTo("SYSTEM");
    }

    @Test
    void getCurrentActor_withHeaders_returnsHeaderActor() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Actor-Email", "admin@example.com");
        request.addHeader("X-Actor-Role", "ADMIN");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var provider = new FallbackAuditActorProvider();

        var actor = provider.getCurrentActor();

        assertThat(actor.email()).isEqualTo("admin@example.com");
        assertThat(actor.role()).isEqualTo("ADMIN");
        assertThat(actor.source()).isEqualTo("HEADER");
    }
}
