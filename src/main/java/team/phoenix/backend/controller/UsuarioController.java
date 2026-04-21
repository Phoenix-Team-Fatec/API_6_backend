package team.phoenix.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.phoenix.backend.controller.dto.CriarUsuarioRequest;
import team.phoenix.backend.controller.dto.UsuarioResponse;
import team.phoenix.backend.domain.model.Usuario;
import team.phoenix.backend.service.UsuarioService;

import java.util.List;
import java.util.stream.Collectors;

// Controlador REST para gerenciamento de usuários
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // POST /api/usuarios - Criar novo usuário
    @PostMapping
    public ResponseEntity<UsuarioResponse> criarUsuario(@RequestBody CriarUsuarioRequest request) {
        try {
            Usuario usuario = usuarioService.criarUsuario(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.from(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /api/usuarios/:id - Buscar usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable String id) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> ResponseEntity.ok(UsuarioResponse.from(usuario)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/usuarios/email/:email - Buscar usuário por email
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioResponse> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
                .map(usuario -> ResponseEntity.ok(UsuarioResponse.from(usuario)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/usuarios - Listar todos os usuários
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        List<UsuarioResponse> usuarios = usuarioService.listarTodos()
                .stream()
                .map(UsuarioResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    // GET /api/usuarios/ativos/lista - Listar usuários ativos
    @GetMapping("/ativos/lista")
    public ResponseEntity<List<UsuarioResponse>> listarAtivos() {
        List<UsuarioResponse> usuarios = usuarioService.listarAtivos()
                .stream()
                .map(UsuarioResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    // GET /api/usuarios/papel/:papel - Listar usuários por papel
    @GetMapping("/papel/{papel}")
    public ResponseEntity<List<UsuarioResponse>> listarPorPapel(@PathVariable String papel) {
        List<UsuarioResponse> usuarios = usuarioService.listarPorPapel(papel)
                .stream()
                .map(UsuarioResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    // PUT /api/usuarios/:id - Atualizar usuário
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable String id, @RequestBody CriarUsuarioRequest request) {
        try {
            Usuario usuario = usuarioService.atualizar(id, request);
            return ResponseEntity.ok(UsuarioResponse.from(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PATCH /api/usuarios/:id/status - Ativar/desativar usuário
    @PatchMapping("/{id}/status")
    public ResponseEntity<UsuarioResponse> alterarStatus(@PathVariable String id, @RequestParam Boolean ativo) {
        try {
            Usuario usuario = usuarioService.alterarStatus(id, ativo);
            return ResponseEntity.ok(UsuarioResponse.from(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/usuarios/:id - Deletar usuário (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        try {
            usuarioService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
