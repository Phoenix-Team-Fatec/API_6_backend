package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.CommissionRate;
import java.util.List;
import java.util.Optional;

public interface CommissionRateRepository extends MongoRepository<CommissionRate, String> {
    Optional<CommissionRate> findByCodMarcaAndCodCargo(Integer codMarca, Integer codCargo);
    List<CommissionRate> findByCodMarca(Integer codMarca);
    List<CommissionRate> findByCodCargo(Integer codCargo);
}
