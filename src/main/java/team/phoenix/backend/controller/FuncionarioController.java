package team.phoenix.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.service.FuncionarioService;

@RestController
@RequestMapping("/api/funcionarios")
@CrossOrigin
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @GetMapping
    public ResponseEntity<?> listActive() {
        return ResponseEntity.ok(funcionarioService.listActive());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable String id) {
        try {
            funcionarioService.softDelete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}