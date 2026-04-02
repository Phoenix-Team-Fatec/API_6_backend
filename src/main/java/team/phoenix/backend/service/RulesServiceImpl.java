package team.phoenix.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;
import java.util.List;

// Implementação do serviço de listagem de regras e exceções mensais
@Service
@RequiredArgsConstructor
public class RulesServiceImpl implements RulesService {

    private final CommissionRateRepository rateRepo;
    private final MonthlyExceptionRepository exceptionRepo;

    // Lista taxas de comissão com filtros opcionais por marca e/ou cargo
    // Parâm codMarca: código da marca (opcional)
    // Parâm codCargo: código do cargo (opcional)
    // Retorna: lista de CommissionRate filtrada ou todas se sem filtro
    @Override
    public List<CommissionRate> listRates(Integer codMarca, Integer codCargo) {
        if (codMarca != null && codCargo != null)
            return rateRepo.findByCodMarcaAndCodCargo(codMarca, codCargo)
                .map(List::of).orElse(List.of());
        if (codMarca != null) return rateRepo.findByCodMarca(codMarca);
        if (codCargo != null) return rateRepo.findByCodCargo(codCargo);
        return rateRepo.findAll();
    }

    // Lista todas as exceções mensais sem filtro
    // Retorna: lista completa de MonthlyException de todos os meses
    @Override
    public List<MonthlyException> listAllExceptions() {
        return exceptionRepo.findAll();
    }

    // Lista exceções mensais de um mês com filtros opcionais
    // Parâm yearMonth: período no formato yyyy-MM
    // Parâm type: tipo de exceção (opcional)
    // Parâm matricula: matrícula do funcionário (opcional)
    // Retorna: lista de MonthlyException filtrada
    @Override
    public List<MonthlyException> listExceptions(String yearMonth, ExceptionType type, String matricula) {
        if (type != null && matricula != null)
            return exceptionRepo.findByYearMonthAndTypeAndMatricula(yearMonth, type, matricula);
        if (type != null) return exceptionRepo.findByYearMonthAndType(yearMonth, type);
        if (matricula != null) return exceptionRepo.findByYearMonthAndMatricula(yearMonth, matricula);
        return exceptionRepo.findByYearMonth(yearMonth);
    }
}
