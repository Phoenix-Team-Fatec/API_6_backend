package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.Usuario;
import java.util.List;
import java.util.Optional;

// Repositório para acesso a usuários no MongoDB
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    // Busca usuário por email
    Optional<Usuario> findByEmail(String email);

    // Lista todos os usuários ativos
    List<Usuario> findByAtivo(Boolean ativo);

    // Lista todos os usuários com um papel específico
    List<Usuario> findByPapel(String papel);

    // Busca usuário ativo por email
    Optional<Usuario> findByEmailAndAtivo(String email, Boolean ativo);
}
