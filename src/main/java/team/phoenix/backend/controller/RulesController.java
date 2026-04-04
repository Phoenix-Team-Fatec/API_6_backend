package team.phoenix.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.service.RulesService;
import java.time.LocalDate;
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

    @PostMapping
    public ResponseEntity<?> createRate(@RequestBody CreateCommissionRateRequest req) {
        try {
            var rate = CommissionRate.builder()
                .codMarca(req.codMarca())
                .descrMarca(req.descrMarca())
                .codCargo(req.codCargo())
                .descriCargo(req.descriCargo())
                .pctComiss(req.pctComiss())
                .data(req.getDataAsLocalDate())
                .build();

            var created = rulesService.createRate(rate);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommissionRateResponse.from(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar regra: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRate(@PathVariable String id, @RequestBody CreateCommissionRateRequest req) {
        try {
            var updated = CommissionRate.builder()
                .codMarca(req.codMarca())
                .descrMarca(req.descrMarca())
                .codCargo(req.codCargo())
                .descriCargo(req.descriCargo())
                .pctComiss(req.pctComiss())
                .data(req.getDataAsLocalDate())
                .build();

            var rate = rulesService.updateRate(id, updated);
            return ResponseEntity.ok(CommissionRateResponse.from(rate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar regra: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRate(@PathVariable String id) {
        try {
            rulesService.deactivateRate(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar regra: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRate(@PathVariable String id) {
        var rate = rulesService.getRateById(id);
        if (rate.isPresent()) {
            return ResponseEntity.ok(CommissionRateResponse.from(rate.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/exceptions")
    public ResponseEntity<?> listExceptions(
            @RequestParam String month,
            @RequestParam(required = false) ExceptionType type,
            @RequestParam(required = false) String matricula) {
        LocalDate referenceDate;
        try {
            referenceDate = YearMonth.parse(month).atDay(1);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid month format. Use yyyy-MM");
        }
        var exceptions = rulesService.listExceptions(referenceDate, type, matricula)
            .stream().map(MonthlyExceptionResponse::from).toList();
        return ResponseEntity.ok(exceptions);
    }
}
