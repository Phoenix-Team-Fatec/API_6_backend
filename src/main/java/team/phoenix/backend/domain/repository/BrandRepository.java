package team.phoenix.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import team.phoenix.backend.domain.model.Brand;

public interface BrandRepository extends MongoRepository<Brand, String> {
    Optional<Brand> findByCodigo(Integer codigo);
    List<Brand> findByNomeContainingIgnoreCase(String nome);
    List<Brand> findByDescricaoContainingIgnoreCase(String descricao);
}
