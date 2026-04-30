package team.phoenix.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import team.phoenix.backend.domain.repository.*;

@SpringBootTest
class BackendApplicationTests {

    @MockitoBean CommissionRateRepository rateRepo;
    @MockitoBean HrRecordRepository hrRepo;
    @MockitoBean SalesRecordRepository salesRepo;
    @MockitoBean MonthlyExceptionRepository exceptionRepo;
    @MockitoBean BrandRepository brandRepository;
    @MockitoBean StoreRepository storeRepository;

    @Test
    void contextLoads() {
    }

}
