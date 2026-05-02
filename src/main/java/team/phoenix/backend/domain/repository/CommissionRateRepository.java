package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import team.phoenix.backend.domain.model.CommissionRate;
import java.util.List;
import java.util.Optional;

// Repositório para acesso a taxas de comissão no MongoDB
public interface CommissionRateRepository extends MongoRepository<CommissionRate, String> {
    // Busca taxa de comissão por marca e cargo
    // Parâm codMarca: código da marca
    // Parâm codCargo: código do cargo
    // Retorna: Optional de CommissionRate
    Optional<CommissionRate> findByCodMarcaAndCodCargo(Integer codMarca, Integer codCargo);

    @Query(value = "{ 'codMarca': ?0, 'codCargo': ?1, 'isVigente': true, 'deletedAt': null }",
        sort = "{ 'versao': -1 }")
    Optional<CommissionRate> findActiveLatestByCodMarcaAndCodCargo(Integer codMarca, Integer codCargo);

    List<CommissionRate> findByCodMarca(Integer codMarca);
    List<CommissionRate> findByCodCargo(Integer codCargo);
}
