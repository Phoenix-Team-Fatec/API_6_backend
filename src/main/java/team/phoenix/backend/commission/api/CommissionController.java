package team.phoenix.backend.commission.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.phoenix.backend.commission.application.CommissionService;
import team.phoenix.backend.commission.application.CommissionCalculationResult;
import team.phoenix.backend.commission.exception.CommissionRateNotFoundException;
import team.phoenix.backend.commission.exception.EmployeeNotFoundException;
import team.phoenix.backend.commission.exception.InvalidCommissionRequestException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

// Controlador REST para simulação de cálculos de comissão
@RestController
@RequestMapping("/api/commission")
@CrossOrigin
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionService commissionService;

    // GET /api/commission/simulate - Simula cálculo de comissão para um mês
    // Parâm matricula: matrícula do funcionário (String)
    // Parâm month: mês no formato yyyy-MM (String)
    // Retorna: ResponseEntity com CommissionResponse ou erro de validação
    @GetMapping("/simulate")
    public ResponseEntity<?> simulate(
            @RequestParam String matricula,
            @RequestParam String month) {
        LocalDate yearMonth;
        try {
            yearMonth = YearMonth.parse(month).atDay(1);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid month format. Use yyyy-MM");
        }
        try {
            return ResponseEntity.ok(
                CommissionResponse.from(commissionService.simulate(matricula, yearMonth)));
        } catch (EmployeeNotFoundException | CommissionRateNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(
            @RequestBody CommissionCalculationRequest request,
            @RequestParam(defaultValue = "true") boolean auditoria) {
        try {
            if (request == null) {
                throw new InvalidCommissionRequestException("Request body is required");
            }
            request.getMonthAsLocalDate();
            CommissionCalculationResult result = commissionService.calculate(request.toCommand(), auditoria);
            return ResponseEntity.ok(CommissionCalculationResponse.from(result));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid month format. Use yyyy-MM");
        } catch (InvalidCommissionRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EmployeeNotFoundException | CommissionRateNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
