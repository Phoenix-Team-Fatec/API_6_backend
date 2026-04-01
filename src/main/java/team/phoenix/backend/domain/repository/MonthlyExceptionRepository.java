package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;
import java.util.List;

// Repositório para acesso a excepções mensais no MongoDB
public interface MonthlyExceptionRepository extends MongoRepository<MonthlyException, String> {
    // Busca excepções por ano-mês
    // Parâm yearMonth: período no formato yyyy-MM
    // Retorna: lista de MonthlyException do mês
    List<MonthlyException> findByYearMonth(String yearMonth);
    List<MonthlyException> findByYearMonthAndType(String yearMonth, ExceptionType type);
    List<MonthlyException> findByYearMonthAndMatricula(String yearMonth, String matricula);
    List<MonthlyException> findByYearMonthAndTypeAndMatricula(String yearMonth, ExceptionType type, String matricula);
}
