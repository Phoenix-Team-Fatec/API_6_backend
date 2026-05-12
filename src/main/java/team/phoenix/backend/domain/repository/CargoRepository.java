package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.Cargo;

public interface CargoRepository extends MongoRepository<Cargo, String> {
    Cargo findByCodCargo(String codCargo);
}
