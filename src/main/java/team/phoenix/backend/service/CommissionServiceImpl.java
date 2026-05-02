package team.phoenix.backend.service;

import java.time.LocalDate;
import java.util.List;

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
import team.phoenix.backend.service.exception.CommissionRateNotFoundException;
import team.phoenix.backend.service.exception.EmployeeNotFoundException;
import team.phoenix.backend.service.exception.InvalidCommissionRequestException;

@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private static final int MANAGER_CARGO = 150;

    private final CommissionRateRepository rateRepo;
    private final HrRecordRepository hrRepo;
    private final SalesRecordRepository salesRepo;
    private final MonthlyExceptionRepository exceptionRepo;
    private final CommissionCalculator calculator;

    @Override
    public CommissionResult simulate(String matricula, LocalDate month) {
        HrRecord hr = hrRepo.findByMatriculaAndDataRef(matricula, month)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "HR record not found: matricula=" + matricula + " month=" + month));

        return calculateEmployee(hr, month);
    }

    @Override
    public CommissionCalculationResult calculate(CommissionCalculationCommand command) {
        validateCommand(command);
        LocalDate month = command.monthAsLocalDate();

        return switch (command.targetType()) {
            case EMPLOYEE -> calculateForEmployee(command, month);
            case STORE -> calculateForStore(command, month);
            case BRAND -> calculateForBrand(command, month);
        };
    }

    private CommissionCalculationResult calculateForEmployee(CommissionCalculationCommand command, LocalDate month) {
        if (command.matricula() == null || command.matricula().isBlank()) {
            throw new InvalidCommissionRequestException("matricula is required for EMPLOYEE target");
        }

        CommissionResult item = simulate(command.matricula(), month);
        return CommissionCalculationResult.from(
            month,
            CommissionTargetType.EMPLOYEE,
            command.matricula(),
            List.of(item)
        );
    }

    private CommissionCalculationResult calculateForStore(CommissionCalculationCommand command, LocalDate month) {
        if (command.codLoja() == null) {
            throw new InvalidCommissionRequestException("codLoja is required for STORE target");
        }

        List<HrRecord> employees = hrRepo.findByCodLojaAndDataRef(command.codLoja(), month);
        if (employees.isEmpty()) {
            throw new EmployeeNotFoundException(
                "No HR records found: codLoja=" + command.codLoja() + " month=" + month);
        }

        List<CommissionResult> items = employees.stream()
            .map(hr -> calculateEmployee(hr, month))
            .toList();

        return CommissionCalculationResult.from(
            month,
            CommissionTargetType.STORE,
            String.valueOf(command.codLoja()),
            items
        );
    }

    private CommissionCalculationResult calculateForBrand(CommissionCalculationCommand command, LocalDate month) {
        if (command.codMarca() == null) {
            throw new InvalidCommissionRequestException("codMarca is required for BRAND target");
        }

        List<HrRecord> employees = hrRepo.findByCodMarcaAndDataRef(command.codMarca(), month);
        if (employees.isEmpty()) {
            throw new EmployeeNotFoundException(
                "No HR records found: codMarca=" + command.codMarca() + " month=" + month);
        }

        List<CommissionResult> items = employees.stream()
            .map(hr -> calculateEmployee(hr, month))
            .toList();

        return CommissionCalculationResult.from(
            month,
            CommissionTargetType.BRAND,
            String.valueOf(command.codMarca()),
            items
        );
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

        CommissionRate rate = rateRepo.findActiveLatestByCodMarcaAndCodCargo(hr.getCodMarca(), hr.getCodCargo())
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
