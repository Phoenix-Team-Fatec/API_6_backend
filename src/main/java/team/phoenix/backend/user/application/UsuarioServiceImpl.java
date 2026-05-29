package team.phoenix.backend.user.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import team.phoenix.backend.config.security.JwtService;
import team.phoenix.backend.domain.model.Usuario;
import team.phoenix.backend.domain.repository.UsuarioRepository;
import team.phoenix.backend.user.api.dto.CriarUsuarioRequest;
import team.phoenix.backend.user.api.dto.LoginRequest;
import team.phoenix.backend.user.api.dto.LoginResponse;
import team.phoenix.backend.user.api.dto.UsuarioResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Implementação do serviço de usuário
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Validar credenciais e retornar token JWT
    @Override
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos"));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Email ou senha inválidos");
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getPapel());

        return LoginResponse.builder()
                .token(token)
                .usuario(UsuarioResponse.from(usuario))
                .build();
    }

    @Override
    public Usuario criarUsuario(CriarUsuarioRequest request) {
        // Validar email único
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        // Validar papel
        if (!request.getPapel().equals("ADMIN") && !request.getPapel().equals("USER")) {
            throw new IllegalArgumentException("Papel inválido. Use ADMIN ou USER");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .papel(request.getPapel())
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public List<Usuario> listarAtivos() {
        return usuarioRepository.findByAtivo(true);
    }

    @Override
    public List<Usuario> listarPorPapel(String papel) {
        return usuarioRepository.findByPapel(papel);
    }

    @Override
    public Usuario atualizar(String id, CriarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Validar se novo email já existe (se foi alterado)
        if (!usuario.getEmail().equals(request.getEmail()) &&
            usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        // Validar papel
        if (!request.getPapel().equals("ADMIN") && !request.getPapel().equals("USER")) {
            throw new IllegalArgumentException("Papel inválido. Use ADMIN ou USER");
        }

        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setPapel(request.getPapel());
        usuario.setAtualizadoEm(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario alterarStatus(String id, Boolean ativo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        usuario.setAtivo(ativo);
        usuario.setAtualizadoEm(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    @Override
    public void deletar(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        usuario.setAtivo(false);
        usuario.setAtualizadoEm(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
}
