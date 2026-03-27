package team.phoenix.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.service.RulesService;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin
@RequiredArgsConstructor
public class RulesController {

    private final RulesService rulesService;

    @GetMapping("/commission-rates")
    public ResponseEntity<List<CommissionRateResponse>> listRates(
            @RequestParam(required = false) Integer codMarca,
            @RequestParam(required = false) Integer codCargo) {
        var rates = rulesService.listRates(codMarca, codCargo)
            .stream().map(CommissionRateResponse::from).toList();
        return ResponseEntity.ok(rates);
    }

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
