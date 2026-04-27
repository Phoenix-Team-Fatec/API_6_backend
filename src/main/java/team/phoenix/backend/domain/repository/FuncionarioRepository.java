package team.phoenix.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import team.phoenix.backend.domain.model.Funcionario;

public interface FuncionarioRepository extends MongoRepository<Funcionario, ObjectId> {
    
    /**
     * Lista todos os funcionários ativos (compatibilidade com código antigo)
     */
    List<Funcionario> findByDeletedAtIsNull();

    /**
     * Lista todos os funcionários com status ativo = true
     */
    List<Funcionario> findByAtivoTrue();

    /**
     * Busca funcionário ativo por matrícula
     */
    Optional<Funcionario> findByMatriculaAndAtivoTrue(String matricula);

    /**
     * Busca funcionário por matrícula (ativo ou inativo)
     */
    Optional<Funcionario> findByMatricula(String matricula);
}