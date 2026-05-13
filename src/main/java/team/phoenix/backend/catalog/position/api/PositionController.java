package team.phoenix.backend.catalog.position.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.domain.model.Position;
import team.phoenix.backend.catalog.position.application.PositionService;

@RestController
@RequestMapping("/api/positions")
@CrossOrigin
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    public ResponseEntity<List<PositionResponse>> listPositions(
            @RequestParam(required = false) Integer codigo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String descricao) {
        var positions = positionService.listPositions(codigo, nome, descricao)
            .stream().map(PositionResponse::from).toList();
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPosition(@PathVariable String id) {
        var position = positionService.getPositionById(id);
        if (position.isPresent()) {
            return ResponseEntity.ok(PositionResponse.from(position.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createPosition(@RequestBody CreatePositionRequest req) {
        try {
            var position = Position.builder()
                .codigo(req.codigo())
                .nome(req.nome())
                .descricao(req.descricao())
                .build();

            var created = positionService.createPosition(position);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(PositionResponse.from(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar cargo: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePosition(@PathVariable String id, @RequestBody CreatePositionRequest req) {
        try {
            var updated = Position.builder()
                .codigo(req.codigo())
                .nome(req.nome())
                .descricao(req.descricao())
                .build();

            var position = positionService.updatePosition(id, updated);
            return ResponseEntity.ok(PositionResponse.from(position));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar cargo: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePosition(@PathVariable String id) {
        try {
            positionService.deletePosition(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar cargo: " + e.getMessage());
        }
    }
}
