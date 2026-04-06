package team.phoenix.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.service.RulesService;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

// Controlador REST para listagem de regras de comissão e exceções mensais
@RestController
@RequestMapping("/api/rules")
@CrossOrigin
@RequiredArgsConstructor
public class RulesController {

    private final RulesService rulesService;

    // GET /api/rules/commission-rates - Lista taxas de comissão com filtros opcionais
    // Parâm codMarca: código da marca (opcional)
    // Parâm codCargo: código do cargo (opcional)
    // Retorna: lista de CommissionRateResponse
    @GetMapping("/commission-rates")
    public ResponseEntity<List<CommissionRateResponse>> listRates(
            @RequestParam(required = false) Integer codMarca,
            @RequestParam(required = false) Integer codCargo) {
        var rates = rulesService.listRates(codMarca, codCargo)
            .stream().map(CommissionRateResponse::from).toList();
        return ResponseEntity.ok(rates);
    }

    // GET /api/rules/exceptions/all - Lista todas as exceções mensais sem filtro
    // Retorna: lista completa de MonthlyExceptionResponse
    @GetMapping("/exceptions/all")
    public ResponseEntity<List<MonthlyExceptionResponse>> listAllExceptions() {
        var exceptions = rulesService.listAllExceptions()
            .stream().map(MonthlyExceptionResponse::from).toList();
        return ResponseEntity.ok(exceptions);
    }

    // GET /api/rules/exceptions - Lista exceções mensais de um mês com filtros opcionais
    // Parâm month: mês no formato yyyy-MM (obrigatório)
    // Parâm type: tipo de exceção (opcional)
    // Parâm matricula: matrícula do funcionário (opcional)
    // Retorna: lista de MonthlyExceptionResponse ou 400 se formato inválido
    @GetMapping("/exceptions")
    public ResponseEntity<?> listExceptions(
            @RequestParam String month,
            @RequestParam(required = false) ExceptionType type,
            @RequestParam(required = false) String matricula) {
        try {
            YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid month format. Use yyyy-MM");
        }
        var exceptions = rulesService.listExceptions(month, type, matricula)
            .stream().map(MonthlyExceptionResponse::from).toList();
        return ResponseEntity.ok(exceptions);
    }
}
