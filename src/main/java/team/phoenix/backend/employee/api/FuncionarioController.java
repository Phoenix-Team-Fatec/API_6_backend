package team.phoenix.backend.employee.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team.phoenix.backend.common.api.ApiResponse;
import team.phoenix.backend.employee.application.FuncionarioService;

@Slf4j
@RestController
@RequestMapping("/api/funcionarios")
@CrossOrigin
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    /**
     * GET /api/funcionarios
     * Lista todos os funcionários ativos sem repetição
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FuncionarioResponse>>> listActive() {
        log.info("Listando todos os funcionários ativos");
        var funcionarios = funcionarioService.listActive();
        var response = funcionarios.stream()
            .map(FuncionarioResponse::fromFuncionario)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Funcionários listados com sucesso", response));
    }

    /**
     * GET /api/funcionarios/{matricula}
     * Busca um funcionário ativo por matrícula
     */
    @GetMapping("/{matricula}")
    public ResponseEntity<?> findByMatricula(@PathVariable String matricula) {
        log.info("Buscando funcionário com matrícula: {}", matricula);
        return funcionarioService.findByMatricula(matricula)
            .map(f -> ResponseEntity.ok(
                ApiResponse.success("Funcionário encontrado", FuncionarioResponse.fromFuncionario(f))
            ))
            .orElse(ResponseEntity.ok(
                ApiResponse.error("Funcionário não encontrado")
            ));
    }

    /**
     * DELETE /api/funcionarios/{id}
     * Soft delete: marca um funcionário como deletado
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable String id) {
        try {
            log.info("Marcando funcionário como deletado: {}", id);
            funcionarioService.softDelete(id);
            return ResponseEntity.ok(ApiResponse.success("Funcionário deletado com sucesso"));
        } catch (IllegalArgumentException ex) {
            log.warn("Funcionário não encontrado: {}", id);
            return ResponseEntity.ok(ApiResponse.error("Funcionário não encontrado"));
        }
    }

    /**
     * POST /api/funcionarios/{id}/reactivate
     * Reativa um funcionário que foi marcado como deletado
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivate(@PathVariable String id) {
        try {
            log.info("Reativando funcionário: {}", id);
            funcionarioService.reactivate(id);
            return ResponseEntity.ok(ApiResponse.success("Funcionário reativado com sucesso"));
        } catch (IllegalArgumentException ex) {
            log.warn("Funcionário não encontrado: {}", id);
            return ResponseEntity.ok(ApiResponse.error("Funcionário não encontrado"));
        }
    }

    /**
     * POST /api/funcionarios/consolidate
     * Consolida dados de hr_records em funcionarios
     * Remove duplicatas pegando o registro mais recente de cada matrícula
     */
    @PostMapping("/consolidate")
    public ResponseEntity<?> consolidate() {
        try {
            log.info("Iniciando consolidação de hr_records");
            funcionarioService.consolidateFromHrRecords();
            return ResponseEntity.ok(
                ApiResponse.success("Consolidação concluída com sucesso")
            );
        } catch (Exception ex) {
            log.error("Erro ao consolidar dados", ex);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Erro ao consolidar: " + ex.getMessage()));
        }
    }
}
