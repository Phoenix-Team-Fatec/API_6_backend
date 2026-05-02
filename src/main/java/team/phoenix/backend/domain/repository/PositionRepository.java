package team.phoenix.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import team.phoenix.backend.domain.model.Position;

public interface PositionRepository extends MongoRepository<Position, String> {
    Optional<Position> findByCodigo(Integer codigo);
    List<Position> findByNomeContainingIgnoreCase(String nome);
    List<Position> findByDescricaoContainingIgnoreCase(String descricao);
}
