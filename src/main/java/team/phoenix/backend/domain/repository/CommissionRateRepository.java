package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.CommissionRate;
import java.util.Optional;

// Repositório para acesso a taxas de comissão no MongoDB
public interface CommissionRateRepository extends MongoRepository<CommissionRate, String> {
    // Busca taxa de comissão por marca e cargo
    // Parâm codMarca: código da marca
    // Parâm codCargo: código do cargo
    // Retorna: Optional de CommissionRate
    Optional<CommissionRate> findByCodMarcaAndCodCargo(Integer codMarca, Integer codCargo);
}
