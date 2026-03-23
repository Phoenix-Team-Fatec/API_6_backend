package team.phoenix.backend.importer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import team.phoenix.backend.domain.repository.*;

@Slf4j
@Component
@Profile("import")
@RequiredArgsConstructor
public class DataImportRunner implements CommandLineRunner {

    private final DataImportService importService;
    private final ExceptionSeeder seeder;
    private final CommissionRateRepository rateRepo;
    private final HrRecordRepository hrRepo;
    private final SalesRecordRepository salesRepo;
    private final MonthlyExceptionRepository exceptionRepo;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Starting data import ===");
        rateRepo.deleteAll();
        hrRepo.deleteAll();
        salesRepo.deleteAll();
        exceptionRepo.deleteAll();

        var rates = importService.readCommissionRates();
        rateRepo.saveAll(rates);
        log.info("commission_rates: {}", rates.size());

        var hrRecords = importService.readHrRecords();
        hrRepo.saveAll(hrRecords);
        log.info("hr_records: {}", hrRecords.size());

        var sales = importService.readSalesRecords();
        salesRepo.saveAll(sales);
        log.info("sales_records: {}", sales.size());

        var exceptions = seeder.buildAll();
        exceptionRepo.saveAll(exceptions);
        log.info("monthly_exceptions: {}", exceptions.size());

        log.info("=== Import complete ===");
    }
}
