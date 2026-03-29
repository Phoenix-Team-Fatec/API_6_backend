package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.SalesRecord;
import java.time.LocalDate;
import java.util.List;

// Repositório para acesso a registros de vendas no MongoDB
public interface SalesRecordRepository extends MongoRepository<SalesRecord, String> {
    // Busca vendas por matrícula e data de referência
    // Parâm matricula: matrícula do funcionário
    // Parâm dateRef: data de referência
    // Retorna: lista de SalesRecord
    List<SalesRecord> findByMatriculaAndDateRef(String matricula, LocalDate dateRef);
    
    // Busca vendas por código de loja e data de referência
    // Parâm codLoja: código da loja
    // Parâm dateRef: data de referência
    // Retorna: lista de SalesRecord
    List<SalesRecord> findByCodLojaAndDateRef(Integer codLoja, LocalDate dateRef);
}
