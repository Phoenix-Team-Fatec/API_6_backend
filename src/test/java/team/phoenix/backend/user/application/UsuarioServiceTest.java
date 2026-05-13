package team.phoenix.backend.user.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import team.phoenix.backend.user.api.dto.CriarUsuarioRequest;
import team.phoenix.backend.domain.model.Usuario;
import team.phoenix.backend.domain.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Testes do Serviço de Usuários")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve criar um novo usuário com sucesso")
    void testarCriarUsuarioComSucesso() {
        // Arrange
        CriarUsuarioRequest request = CriarUsuarioRequest.builder()
                .nome("João Silva")
                .email("joao@example.com")
                .senha("senha123")
                .papel("USER")
                .build();

        Usuario usuarioEsperado = Usuario.builder()
                .id("123")
                .nome("João Silva")
                .email("joao@example.com")
                .senha("encodedSenha")
                .papel("USER")
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("encodedSenha");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioEsperado);

        // Act
        Usuario usuarioCriado = usuarioService.criarUsuario(request);

        // Assert
        assertNotNull(usuarioCriado);
        assertEquals("João Silva", usuarioCriado.getNome());
        assertEquals("joao@example.com", usuarioCriado.getEmail());
        assertEquals("USER", usuarioCriado.getPapel());
        assertTrue(usuarioCriado.getAtivo());

        verify(usuarioRepository, times(1)).findByEmail("joao@example.com");
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email duplicado")
    void testarCriarUsuarioComEmailDuplicado() {
        // Arrange
        CriarUsuarioRequest request = CriarUsuarioRequest.builder()
                .nome("João Silva")
                .email("joao@example.com")
                .senha("senha123")
                .papel("USER")
                .build();

        Usuario usuarioExistente = Usuario.builder()
                .id("456")
                .nome("Outro Usuário")
                .email("joao@example.com")
                .build();

        when(usuarioRepository.findByEmail("joao@example.com"))
                .thenReturn(Optional.of(usuarioExistente));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.criarUsuario(request)
        );

        assertEquals("Email já cadastrado", exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail("joao@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com papel inválido")
    void testarCriarUsuarioComPapelInvalido() {
        // Arrange
        CriarUsuarioRequest request = CriarUsuarioRequest.builder()
                .nome("João Silva")
                .email("joao@example.com")
                .senha("senha123")
                .papel("SUPER_ADMIN")
                .build();

        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.criarUsuario(request)
        );

        assertEquals("Papel inválido. Use ADMIN ou USER", exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail("joao@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void testarBuscarPorIdComSucesso() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id("123")
                .nome("João Silva")
                .email("joao@example.com")
                .papel("USER")
                .ativo(true)
                .build();

        when(usuarioRepository.findById("123")).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> usuarioEncontrado = usuarioService.buscarPorId("123");

        // Assert
        assertTrue(usuarioEncontrado.isPresent());
        assertEquals("João Silva", usuarioEncontrado.get().getNome());

        verify(usuarioRepository, times(1)).findById("123");
    }

    @Test
    @DisplayName("Deve deletar usuário (soft delete)")
    void testarDeletarUsuario() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id("123")
                .nome("João Silva")
                .email("joao@example.com")
                .papel("USER")
                .ativo(true)
                .build();

        Usuario usuarioDeletado = Usuario.builder()
                .id("123")
                .nome("João Silva")
                .email("joao@example.com")
                .papel("USER")
                .ativo(false)
                .atualizadoEm(LocalDateTime.now())
                .build();

        when(usuarioRepository.findById("123")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioDeletado);

        // Act
        usuarioService.deletar("123");

        // Assert
        verify(usuarioRepository, times(1)).findById("123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar usuário inexistente")
    void testarDeletarUsuarioInexistente() {
        // Arrange
        when(usuarioRepository.findById("999")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.deletar("999")
        );

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usuarioRepository, times(1)).findById("999");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
