package team.phoenix.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.phoenix.backend.service.CommissionService;
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
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid month format. Use yyyy-MM");
        }
        try {
            return ResponseEntity.ok(
                CommissionResponse.from(commissionService.simulate(matricula, yearMonth)));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
