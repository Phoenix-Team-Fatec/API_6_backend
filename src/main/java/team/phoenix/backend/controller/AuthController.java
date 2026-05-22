package team.phoenix.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import team.phoenix.backend.config.JwtUtils;
import team.phoenix.backend.controller.dto.CriarUsuarioRequest;
import team.phoenix.backend.controller.dto.LoginRequest;
import team.phoenix.backend.controller.dto.LoginResponse;
import team.phoenix.backend.domain.model.Usuario;
import team.phoenix.backend.domain.repository.UsuarioRepository;
import team.phoenix.backend.service.UsuarioService;

// Controlador REST para autenticação
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    /**
     * POST /api/auth/login - Autentica um usuário e retorna um token JWT
     * @param request Email e senha do usuário
     * @return Token JWT com dados do usuário autenticado
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Autenticar com AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getSenha()
                    )
            );

            // Buscar usuário para retornar seus dados
            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Gerar token JWT
            String token = jwtUtils.generateToken(authentication);

            // Retornar resposta com token
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .tipo("Bearer")
                    .id(usuario.getId())
                    .nome(usuario.getNome())
                    .email(usuario.getEmail())
                    .papel(usuario.getPapel())
                    .build();

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            System.err.println("Erro de autenticação: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro no login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro no login: " + e.getMessage());
        }
    }

    /**
     * POST /api/auth/register - Registra um novo usuário (público)
     * @param request Nome, email, senha e papel
     * @return Dados do usuário criado
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CriarUsuarioRequest request) {
        try {
            Usuario usuario = usuarioService.criarUsuario(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro no registro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro no registro: " + e.getMessage());
        }
    }
}
