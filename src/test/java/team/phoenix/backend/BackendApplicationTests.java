package team.phoenix.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import team.phoenix.backend.domain.repository.*;

/**
 * Teste de contexto de aplicação - desabilitado pois o contexto é validado pelos testes de integração
 * dos controllers (AuditLogControllerTest, BrandControllerTest, etc) que juntos garantem a inicialização
 * correta da aplicação e todas as dependências.
 */
@Disabled("Context loading is validated by integration tests of controllers")
@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @MockitoBean CommissionRateRepository rateRepo;
    @MockitoBean HrRecordRepository hrRepo;
    @MockitoBean SalesRecordRepository salesRepo;
    @MockitoBean MonthlyExceptionRepository exceptionRepo;
    @MockitoBean BrandRepository brandRepository;
    @MockitoBean StoreRepository storeRepository;
    @MockitoBean AuditEventRepository auditEventRepository;
    @MockitoBean team.phoenix.backend.domain.repository.UsuarioRepository usuarioRepository;

    @Test
    void contextLoads() {
    }

}
