package team.phoenix.backend.user.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import team.phoenix.backend.user.api.dto.CriarUsuarioRequest;
import team.phoenix.backend.user.api.dto.UsuarioResponse;
import team.phoenix.backend.domain.model.Usuario;
import team.phoenix.backend.user.application.UsuarioService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Testes do Controlador de Usuários")
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve criar usuário e retornar status CREATED")
    void testarCriarUsuarioComSucesso() {
        // Arrange
        CriarUsuarioRequest request = CriarUsuarioRequest.builder()
                .nome("João Silva")
                .email("joao@example.com")
                .senha("senha123")
                .papel("USER")
                .build();

        Usuario usuarioCriado = Usuario.builder()
                .id("123")
                .nome("João Silva")
                .email("joao@example.com")
                .senha("encodedSenha")
                .papel("USER")
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        when(usuarioService.criarUsuario(any(CriarUsuarioRequest.class)))
                .thenReturn(usuarioCriado);

        // Act
        ResponseEntity<UsuarioResponse> response = usuarioController.criarUsuario(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("João Silva", response.getBody().getNome());
        assertEquals("joao@example.com", response.getBody().getEmail());

        verify(usuarioService, times(1)).criarUsuario(any(CriarUsuarioRequest.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void testarBuscarPorIdComSucesso() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id("123")
                .nome("João Silva")
                .email("joao@example.com")
                .papel("USER")
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        when(usuarioService.buscarPorId("123")).thenReturn(Optional.of(usuario));

        // Act
        ResponseEntity<UsuarioResponse> response = usuarioController.buscarPorId("123");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("João Silva", response.getBody().getNome());

        verify(usuarioService, times(1)).buscarPorId("123");
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar usuário inexistente")
    void testarBuscarPorIdNaoEncontrado() {
        // Arrange
        when(usuarioService.buscarPorId("999")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UsuarioResponse> response = usuarioController.buscarPorId("999");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(usuarioService, times(1)).buscarPorId("999");
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void testarListarTodos() {
        // Arrange
        Usuario usuario1 = Usuario.builder()
                .id("123")
                .nome("João Silva")
                .email("joao@example.com")
                .papel("USER")
                .ativo(true)
                .build();

        Usuario usuario2 = Usuario.builder()
                .id("456")
                .nome("Maria Santos")
                .email("maria@example.com")
                .papel("ADMIN")
                .ativo(true)
                .build();

        List<Usuario> usuarios = Arrays.asList(usuario1, usuario2);

        when(usuarioService.listarTodos()).thenReturn(usuarios);

        // Act
        ResponseEntity<List<UsuarioResponse>> response = usuarioController.listarTodos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(usuarioService, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Deve listar usuários ativos")
    void testarListarAtivos() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id("123")
                .nome("João Silva")
                .email("joao@example.com")
                .papel("USER")
                .ativo(true)
                .build();

        List<Usuario> usuariosAtivos = Arrays.asList(usuario);

        when(usuarioService.listarAtivos()).thenReturn(usuariosAtivos);

        // Act
        ResponseEntity<List<UsuarioResponse>> response = usuarioController.listarAtivos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(usuarioService, times(1)).listarAtivos();
    }

    @Test
    @DisplayName("Deve listar usuários por papel")
    void testarListarPorPapel() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id("456")
                .nome("Maria Santos")
                .email("maria@example.com")
                .papel("ADMIN")
                .ativo(true)
                .build();

        List<Usuario> admins = Arrays.asList(usuario);

        when(usuarioService.listarPorPapel("ADMIN")).thenReturn(admins);

        // Act
        ResponseEntity<List<UsuarioResponse>> response = usuarioController.listarPorPapel("ADMIN");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ADMIN", response.getBody().get(0).getPapel());

        verify(usuarioService, times(1)).listarPorPapel("ADMIN");
    }

    @Test
    @DisplayName("Deve atualizar usuário")
    void testarAtualizarUsuario() {
        // Arrange
        CriarUsuarioRequest request = CriarUsuarioRequest.builder()
                .nome("João Silva Atualizado")
                .email("joao.novo@example.com")
                .senha("novaSenha123")
                .papel("ADMIN")
                .build();

        Usuario usuarioAtualizado = Usuario.builder()
                .id("123")
                .nome("João Silva Atualizado")
                .email("joao.novo@example.com")
                .papel("ADMIN")
                .ativo(true)
                .atualizadoEm(LocalDateTime.now())
                .build();

        when(usuarioService.atualizar(eq("123"), any(CriarUsuarioRequest.class)))
                .thenReturn(usuarioAtualizado);

        // Act
        ResponseEntity<UsuarioResponse> response = usuarioController.atualizar("123", request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("João Silva Atualizado", response.getBody().getNome());
        assertEquals("ADMIN", response.getBody().getPapel());

        verify(usuarioService, times(1)).atualizar(eq("123"), any(CriarUsuarioRequest.class));
    }

    @Test
    @DisplayName("Deve deletar usuário")
    void testarDeletarUsuario() {
        // Arrange
        doNothing().when(usuarioService).deletar("123");

        // Act
        ResponseEntity<Void> response = usuarioController.deletar("123");

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(usuarioService, times(1)).deletar("123");
    }

    @Test
    @DisplayName("Deve retornar 404 ao deletar usuário inexistente")
    void testarDeletarUsuarioInexistente() {
        // Arrange
        doThrow(new IllegalArgumentException("Usuário não encontrado"))
                .when(usuarioService).deletar("999");

        // Act
        ResponseEntity<Void> response = usuarioController.deletar("999");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(usuarioService, times(1)).deletar("999");
    }

    @Test
    @DisplayName("Deve alternar status do usuário")
    void testarAlterarStatus() {
        // Arrange
        Usuario usuarioAtualizado = Usuario.builder()
                .id("123")
                .nome("João Silva")
                .email("joao@example.com")
                .papel("USER")
                .ativo(false)
                .atualizadoEm(LocalDateTime.now())
                .build();

        when(usuarioService.alterarStatus(eq("123"), eq(false)))
                .thenReturn(usuarioAtualizado);

        // Act
        ResponseEntity<UsuarioResponse> response = usuarioController.alterarStatus("123", false);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getAtivo());

        verify(usuarioService, times(1)).alterarStatus("123", false);
    }
}
