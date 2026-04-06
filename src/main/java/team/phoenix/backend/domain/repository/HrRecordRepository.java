package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.HrRecord;
import java.time.LocalDate;
import java.util.Optional;

// Repositório para acesso a registros de RH no MongoDB
public interface HrRecordRepository extends MongoRepository<HrRecord, String> {
    // Busca registro de RH por matrícula e data de referência
    // Parâm matricula: matrícula do funcionário
    // Parâm dataRef: data de referência
    // Retorna: Optional de HrRecord
    Optional<HrRecord> findByMatriculaAndDataRef(String matricula, LocalDate dataRef);
}
