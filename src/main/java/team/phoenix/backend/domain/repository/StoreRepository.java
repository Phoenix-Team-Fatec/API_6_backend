package team.phoenix.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import team.phoenix.backend.domain.model.Store;

public interface StoreRepository extends MongoRepository<Store, String> {
    Optional<Store> findByCodigo(Integer codigo);
    List<Store> findByNomeContainingIgnoreCase(String nome);
    List<Store> findByDescricaoContainingIgnoreCase(String descricao);
}
