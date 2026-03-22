package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.SalesRecord;
import java.time.LocalDate;
import java.util.List;

public interface SalesRecordRepository extends MongoRepository<SalesRecord, String> {
    List<SalesRecord> findByMatriculaAndDateRef(String matricula, LocalDate dateRef);
    List<SalesRecord> findByCodLojaAndDateRef(Integer codLoja, LocalDate dateRef);
}
