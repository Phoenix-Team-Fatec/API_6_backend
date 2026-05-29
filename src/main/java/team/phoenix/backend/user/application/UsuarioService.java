package team.phoenix.backend.user.application;

import team.phoenix.backend.domain.model.Usuario;
import team.phoenix.backend.user.api.dto.CriarUsuarioRequest;
import team.phoenix.backend.user.api.dto.LoginRequest;
import team.phoenix.backend.user.api.dto.LoginResponse;
import java.util.List;
import java.util.Optional;

// Interface do serviço de usuário
public interface UsuarioService {
    // Autenticar usuário e retornar token JWT
    LoginResponse login(LoginRequest request);

    // Criar novo usuário
    Usuario criarUsuario(CriarUsuarioRequest request);

    // Buscar usuário por ID
    Optional<Usuario> buscarPorId(String id);

    // Buscar usuário por email
    Optional<Usuario> buscarPorEmail(String email);

    // Listar todos os usuários ativos
    List<Usuario> listarAtivos();

    // Listar usuários por papel
    List<Usuario> listarPorPapel(String papel);

    // Atualizar usuário
    Usuario atualizar(String id, CriarUsuarioRequest request);

    // Ativar/desativar usuário
    Usuario alterarStatus(String id, Boolean ativo);

    // Deletar usuário (soft delete)
    void deletar(String id);

    // Listar todos os usuários
    List<Usuario> listarTodos();
}
