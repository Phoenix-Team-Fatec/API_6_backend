package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.HrRecord;
import java.time.LocalDate;
import java.util.Optional;

public interface HrRecordRepository extends MongoRepository<HrRecord, String> {
    Optional<HrRecord> findByMatriculaAndDataRef(String matricula, LocalDate dataRef);
}
