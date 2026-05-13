package team.phoenix.backend.rules.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.rules.application.RulesService;
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
            @RequestParam(required = false) Integer codCargo,
            @RequestParam(required = false) Boolean isVigente) {
        var rates = rulesService.listRates(codMarca, codCargo, isVigente)
            .stream().map(CommissionRateResponse::from).toList();
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/commission-rates/trash")
    public ResponseEntity<List<CommissionRateResponse>> listTrashedRates(
            @RequestParam(required = false) Integer codMarca,
            @RequestParam(required = false) Integer codCargo,
            @RequestParam(required = false) Boolean isVigente) {
        var rates = rulesService.listTrashedRates(codMarca, codCargo, isVigente)
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
                .textoOriginal(req.textoOriginal())
                .explicacao(req.explicacao())
                .build();

            var created = rulesService.createRate(rate);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommissionRateResponse.from(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar regra: " + e.getMessage());
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateRate(@RequestBody GenerateRuleRequest req) {
        try {
            var result = rulesService.generateFromNaturalLanguage(req.prompt());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(GeneratedRuleResponse.from(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao gerar regra com IA: " + e.getMessage());
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
                .data(req.getDataAsLocalDateOrNull())
                .textoOriginal(req.textoOriginal())
                .explicacao(req.explicacao())
                .build();

            var rate = rulesService.updateRate(id, updated);
            return ResponseEntity.ok(CommissionRateResponse.from(rate));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Regra não encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body("Erro ao atualizar regra: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar regra: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRate(@PathVariable String id) {
        try {
            rulesService.softDeleteRate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar regra: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateRate(@PathVariable String id) {
        try {
            rulesService.activateRate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao ativar regra: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateRate(@PathVariable String id) {
        try {
            rulesService.deactivateRate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao desativar regra: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<?> restoreRate(@PathVariable String id) {
        try {
            rulesService.restoreRate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao restaurar regra: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/rollback")
    public ResponseEntity<?> rollbackRate(@PathVariable String id) {
        try {
            rulesService.rollbackRate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao reverter regra: " + e.getMessage());
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
