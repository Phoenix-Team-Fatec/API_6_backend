package team.phoenix.backend.domain.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import team.phoenix.backend.domain.model.Funcionario;

public interface FuncionarioRepository extends MongoRepository<Funcionario, ObjectId> {
    List<Funcionario> findByDeletedAtIsNull();
}