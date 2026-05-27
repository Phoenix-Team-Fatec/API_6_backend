package team.phoenix.backend.commission.application;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.HrRecord;
import team.phoenix.backend.domain.model.MonthlyException;
import team.phoenix.backend.domain.model.SalesRecord;
import team.phoenix.backend.domain.repository.CommissionRateRepository;
import team.phoenix.backend.domain.repository.HrRecordRepository;
import team.phoenix.backend.domain.repository.MonthlyExceptionRepository;
import team.phoenix.backend.domain.repository.SalesRecordRepository;
import team.phoenix.backend.commission.domain.CommissionCalculator;
import team.phoenix.backend.commission.domain.CommissionResult;
import team.phoenix.backend.commission.exception.CommissionRateNotFoundException;
import team.phoenix.backend.commission.exception.EmployeeNotFoundException;
import team.phoenix.backend.commission.exception.InvalidCommissionRequestException;

@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private static final int MANAGER_CARGO = 150;

    private final CommissionRateRepository rateRepo;
    private final HrRecordRepository hrRepo;
    private final SalesRecordRepository salesRepo;
    private final MonthlyExceptionRepository exceptionRepo;
    private final CommissionCalculator calculator;
    private final CommissionAiClient aiClient;

    @Override
    public CommissionResult simulate(String matricula, LocalDate month) {
        HrRecord hr = hrRepo.findByMatriculaAndDataRef(matricula, month)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "HR record not found: matricula=" + matricula + " month=" + month));

        return calculateEmployee(hr, month);
    }

    @Override
    public CommissionCalculationResult calculate(CommissionCalculationCommand command) {
        return calculate(command, true);
    }

    @Override
    public CommissionCalculationResult calculate(CommissionCalculationCommand command, boolean auditoria) {
        validateCommand(command);
        LocalDate month = command.monthAsLocalDate();

        List<HrRecord> employees = resolveEmployees(command, month);
        List<SalesRecord> sales = collectSalesForEmployees(employees, month);
        List<CommissionRate> monthlyOverrides = collectMonthlyOverridesForEmployees(employees, month);
        List<CommissionRate> rates = collectRatesForEmployees(employees, monthlyOverrides);
        List<MonthlyException> exceptions = exceptionRepo.findByYearMonth(month);

        var aiRequest = AiCommissionRequest.from(employees, sales, rates, exceptions, monthlyOverrides, month);
        var aiResults = aiClient.calculate(aiRequest, month.getYear(), month.getMonthValue(), auditoria);
        var items = mapAiResults(aiResults, employees, month);
        
        // Extrai etapas do primeiro resultado (se houver)
        List<EtapaCalculo> etapas = aiResults.isEmpty() || aiResults.get(0).etapas() == null 
            ? List.of() 
            : aiResults.get(0).etapas();

        return CommissionCalculationResult.from(
            month,
            command.targetType(),
            resolveTargetId(command),
            items,
            mapAppliedRuleDetails(rates),
            etapas
        );
    }

    private List<HrRecord> resolveEmployees(CommissionCalculationCommand command, LocalDate month) {
        return switch (command.targetType()) {
            case EMPLOYEE -> List.of(resolveEmployee(command, month));
            case STORE -> resolveStoreEmployees(command, month);
            case BRAND -> resolveBrandEmployees(command, month);
        };
    }

    private HrRecord resolveEmployee(CommissionCalculationCommand command, LocalDate month) {
        if (command.matricula() == null || command.matricula().isBlank()) {
            throw new InvalidCommissionRequestException("matricula is required for EMPLOYEE target");
        }

        return hrRepo.findByMatriculaAndDataRef(command.matricula(), month)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "HR record not found: matricula=" + command.matricula() + " month=" + month));
    }

    private List<HrRecord> resolveStoreEmployees(CommissionCalculationCommand command, LocalDate month) {
        if (command.codLoja() == null) {
            throw new InvalidCommissionRequestException("codLoja is required for STORE target");
        }

        List<HrRecord> employees = hrRepo.findByCodLojaAndDataRef(command.codLoja(), month);
        if (employees.isEmpty()) {
            throw new EmployeeNotFoundException(
                "No HR records found: codLoja=" + command.codLoja() + " month=" + month);
        }

        return employees;
    }

    private List<HrRecord> resolveBrandEmployees(CommissionCalculationCommand command, LocalDate month) {
        if (command.codMarca() == null) {
            throw new InvalidCommissionRequestException("codMarca is required for BRAND target");
        }

        List<HrRecord> employees = hrRepo.findByCodMarcaAndDataRef(command.codMarca(), month);
        if (employees.isEmpty()) {
            throw new EmployeeNotFoundException(
                "No HR records found: codMarca=" + command.codMarca() + " month=" + month);
        }

        return employees;
    }

    private List<SalesRecord> collectSalesForEmployees(List<HrRecord> employees, LocalDate month) {
        return employees.stream()
            .map(HrRecord::getCodLoja)
            .filter(Objects::nonNull)
            .distinct()
            .flatMap(codLoja -> salesRepo.findByCodLojaAndDateRef(codLoja, month).stream())
            .toList();
    }

    private List<CommissionRate> collectRatesForEmployees(List<HrRecord> employees, List<CommissionRate> monthlyOverrides) {
        Map<String, CommissionRate> rates = new LinkedHashMap<>();
        Map<String, CommissionRate> overridesByKey = monthlyOverrides.stream()
            .collect(Collectors.toMap(
                rate -> rate.getCodMarca() + ":" + rate.getCodCargo(),
                Function.identity(),
                (left, right) -> right,
                LinkedHashMap::new
            ));

        for (HrRecord employee : employees) {
            String key = employee.getCodMarca() + ":" + employee.getCodCargo();
            if (rates.containsKey(key)) {
                continue;
            }
            if (overridesByKey.containsKey(key)) {
                rates.put(key, overridesByKey.get(key));
                continue;
            }

            CommissionRate rate = rateRepo
                .findFirstByCodMarcaAndCodCargoAndIsVigenteTrueAndDeletedAtNullOrderByVersaoDesc(employee.getCodMarca(), employee.getCodCargo())
                .orElseThrow(() -> new CommissionRateNotFoundException(
                    "Commission rate not found: cod_marca=" + employee.getCodMarca()
                        + " cod_cargo=" + employee.getCodCargo()));
            rates.put(key, rate);
        }
        return List.copyOf(rates.values());
    }

    private List<CommissionRate> collectMonthlyOverridesForEmployees(List<HrRecord> employees, LocalDate month) {
        var employeeKeys = employees.stream()
            .map(employee -> employee.getCodMarca() + ":" + employee.getCodCargo())
            .collect(Collectors.toSet());

        List<CommissionRate> monthRates = rateRepo.findByDataAndIsVigenteTrueAndDeletedAtNull(month);
        if (monthRates == null) {
            monthRates = List.of();
        }

        return monthRates.stream()
            .filter(rate -> rate.getCodMarca() != null && rate.getCodCargo() != null)
            .filter(rate -> employeeKeys.contains(rate.getCodMarca() + ":" + rate.getCodCargo()))
            .toList();
    }

    private List<AppliedRuleDetail> mapAppliedRuleDetails(List<CommissionRate> rates) {
        return rates.stream()
            .map(this::mapAppliedRuleDetail)
            .toList();
    }

    private AppliedRuleDetail mapAppliedRuleDetail(CommissionRate rate) {
        return new AppliedRuleDetail(
            rate.getId(),
            rate.getNomeRegra(),
            "COMMISSION_RATE",
            resolveRateDescription(rate)
        );
    }

    private String resolveRateDescription(CommissionRate rate) {
        if (rate.getTextoOriginal() != null && !rate.getTextoOriginal().isBlank()) {
            return rate.getTextoOriginal();
        }
        return rate.getExplicacao();
    }

    private List<CommissionResult> mapAiResults(
            List<AiCommissionResult> aiResults,
            List<HrRecord> employees,
            LocalDate month) {
        Map<String, HrRecord> employeesByMatricula = employees.stream()
            .collect(Collectors.toMap(HrRecord::getMatricula, Function.identity(), (left, right) -> left));

        return aiResults.stream()
            .map(result -> mapAiResult(result, employeesByMatricula, month))
            .toList();
    }

    private CommissionResult mapAiResult(
            AiCommissionResult result,
            Map<String, HrRecord> employeesByMatricula,
            LocalDate month) {
        HrRecord employee = employeesByMatricula.get(result.matricula());
        if (employee == null) {
            throw new IllegalStateException("AI returned unknown matricula: " + result.matricula());
        }

        double totalBonuses = result.bonus();
        List<String> bonuses = totalBonuses == 0.0
            ? List.of()
            : List.of("AI_BONUS=" + totalBonuses);

        return new CommissionResult(
            result.matricula(),
            month,
            employee,
            result.baseVendas(),
            result.percComissao(),
            result.valorComissaoBruto(),
            bonuses,
            totalBonuses,
            result.valorFinal(),
            "IA_COMMISSION_ALGORITHM",
            "Calculated by API_6_ML /commission-algorithm"
        );
    }

    private String resolveTargetId(CommissionCalculationCommand command) {
        return switch (command.targetType()) {
            case EMPLOYEE -> command.matricula();
            case STORE -> String.valueOf(command.codLoja());
            case BRAND -> String.valueOf(command.codMarca());
        };
    }

    private CommissionResult calculateEmployee(HrRecord hr, LocalDate month) {
        double individualSales = salesRepo.findByMatriculaAndDateRef(hr.getMatricula(), month)
            .stream()
            .mapToDouble(SalesRecord::getVlrVenda)
            .sum();
        double storeSales = salesRepo.findByCodLojaAndDateRef(hr.getCodLoja(), month)
            .stream()
            .mapToDouble(SalesRecord::getVlrVenda)
            .sum();
        List<MonthlyException> exceptions = exceptionRepo.findByYearMonth(month);
        double adjustedStoreSales = resolveStoreSales(hr, storeSales, exceptions, month);

        CommissionRate rate = rateRepo.findFirstByCodMarcaAndCodCargoAndIsVigenteTrueAndDeletedAtNullOrderByVersaoDesc(hr.getCodMarca(), hr.getCodCargo())
            .orElseThrow(() -> new CommissionRateNotFoundException(
                "Commission rate not found: cod_marca=" + hr.getCodMarca()
                    + " cod_cargo=" + hr.getCodCargo()));

        return calculator.calculate(hr, individualSales, adjustedStoreSales, rate.getPctComiss(), exceptions, month);
    }

    private double resolveStoreSales(HrRecord hr, double baseStoreSales,
                                     List<MonthlyException> exceptions, LocalDate month) {
        if (!Integer.valueOf(MANAGER_CARGO).equals(hr.getCodCargo())) {
            return baseStoreSales;
        }

        double adjusted = baseStoreSales;
        for (MonthlyException exception : exceptions) {
            if (exception.getType() != ExceptionType.MULTI_STORE) {
                continue;
            }
            if (!hr.getMatricula().equals(exception.getMatricula())) {
                continue;
            }
            if (exception.getAlternateCodLoja() == null || exception.getDaysWorked() == null) {
                continue;
            }

            double alternateStoreSales = salesRepo
                .findByCodLojaAndDateRef(exception.getAlternateCodLoja(), month)
                .stream()
                .mapToDouble(SalesRecord::getVlrVenda)
                .sum();
            adjusted += alternateStoreSales * ((double) exception.getDaysWorked() / month.lengthOfMonth());
        }
        return adjusted;
    }

    private void validateCommand(CommissionCalculationCommand command) {
        if (command == null) {
            throw new InvalidCommissionRequestException("Request body is required");
        }
        if (command.month() == null || command.month().isBlank()) {
            throw new InvalidCommissionRequestException("month is required");
        }
        if (command.targetType() == null) {
            throw new InvalidCommissionRequestException("targetType is required");
        }
    }
}
