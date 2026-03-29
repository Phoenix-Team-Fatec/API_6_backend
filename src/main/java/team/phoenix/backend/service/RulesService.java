package team.phoenix.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RulesService {

    private final CommissionRateRepository rateRepo;
    private final MonthlyExceptionRepository exceptionRepo;

    public List<CommissionRate> listRates(Integer codMarca, Integer codCargo) {
        if (codMarca != null && codCargo != null)
            return rateRepo.findByCodMarcaAndCodCargo(codMarca, codCargo)
                .map(List::of).orElse(List.of());
        if (codMarca != null) return rateRepo.findByCodMarca(codMarca);
        if (codCargo != null) return rateRepo.findByCodCargo(codCargo);
        return rateRepo.findAll();
    }

    public List<MonthlyException> listExceptions(String yearMonth, ExceptionType type, String matricula) {
        if (type != null && matricula != null)
            return exceptionRepo.findByYearMonthAndTypeAndMatricula(yearMonth, type, matricula);
        if (type != null) return exceptionRepo.findByYearMonthAndType(yearMonth, type);
        if (matricula != null) return exceptionRepo.findByYearMonthAndMatricula(yearMonth, matricula);
        return exceptionRepo.findByYearMonth(yearMonth);
    }
}
