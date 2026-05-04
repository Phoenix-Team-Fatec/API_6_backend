package team.phoenix.backend.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import team.phoenix.backend.domain.model.CommissionRate;

public interface CommissionRateRepository extends MongoRepository<CommissionRate, String> {
    List<CommissionRate> findByCodMarcaAndCodCargo(Integer codMarca, Integer codCargo);

    Optional<CommissionRate> findFirstByCodMarcaAndCodCargoAndIsVigenteTrueAndDeletedAtNullOrderByVersaoDesc(
        Integer codMarca,
        Integer codCargo
    );

    List<CommissionRate> findByCodMarca(Integer codMarca);

    List<CommissionRate> findByCodCargo(Integer codCargo);

    List<CommissionRate> findByDataAndIsVigenteTrueAndDeletedAtNull(LocalDate data);
}
