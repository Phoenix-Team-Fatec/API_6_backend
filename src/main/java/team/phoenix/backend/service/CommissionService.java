package team.phoenix.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommissionService {

    private final CommissionRateRepository rateRepo;
    private final HrRecordRepository hrRepo;
    private final SalesRecordRepository salesRepo;
    private final MonthlyExceptionRepository exceptionRepo;
    private final CommissionCalculator calculator;

    public CommissionResult simulate(String matricula, YearMonth month) {
        LocalDate ref = month.atDay(1);

        HrRecord hr = hrRepo.findByMatriculaAndDataRef(matricula, ref)
            .orElseThrow(() -> new RuntimeException(
                "HR record not found: matricula=" + matricula + " month=" + month));

        double indSales = salesRepo.findByMatriculaAndDateRef(matricula, ref)
            .stream().mapToDouble(SalesRecord::getVlrVenda).sum();
        double storeSales = salesRepo.findByCodLojaAndDateRef(hr.getCodLoja(), ref)
            .stream().mapToDouble(SalesRecord::getVlrVenda).sum();

        CommissionRate rate = rateRepo.findByCodMarcaAndCodCargo(hr.getCodMarca(), hr.getCodCargo())
            .orElseThrow(() -> new RuntimeException(
                "Commission rate not found: cod_marca=" + hr.getCodMarca()
                    + " cod_cargo=" + hr.getCodCargo()));

        List<MonthlyException> exceptions = exceptionRepo.findByYearMonth(month.toString());
        return calculator.calculate(hr, indSales, storeSales, rate.getPctComiss(), exceptions, month);
    }
}
