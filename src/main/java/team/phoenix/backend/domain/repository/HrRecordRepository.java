package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import team.phoenix.backend.domain.model.HrRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HrRecordRepository extends MongoRepository<HrRecord, String> {
    
    /**
     * Busca um registro de RH por matrícula e data de referência
     */
    Optional<HrRecord> findByMatriculaAndDataRef(String matricula, LocalDate dataRef);

    List<HrRecord> findByDataRef(LocalDate dataRef);

    List<HrRecord> findByCodLojaAndDataRef(Integer codLoja, LocalDate dataRef);

    List<HrRecord> findByCodMarcaAndDataRef(Integer codMarca, LocalDate dataRef);

    /**
     * Busca todos os registros por matrícula
     */
    List<HrRecord> findByMatriculaOrderByDataRefDesc(String matricula);

    /**
     * Retorna apenas o registro mais recente de cada funcionário (remove duplicatas)
     * Usando aggregation pipeline do MongoDB
     */
    @Aggregation(pipeline = {
        "{ $sort: { matricula: 1, dataRef: -1 } }",
        "{ $group: { _id: '$matricula', doc: { $first: '$$ROOT' } } }",
        "{ $replaceRoot: { newRoot: '$doc' } }",
        "{ $sort: { matricula: 1 } }"
    })
    List<HrRecord> findLatestByEachMatricula();

    /**
     * Busca o registro mais recente de um funcionário específico
     */
    @Query(value = "{ 'matricula': ?0 }", sort = "{ 'dataRef': -1 }")
    Optional<HrRecord> findLatestByMatricula(String matricula);
}
