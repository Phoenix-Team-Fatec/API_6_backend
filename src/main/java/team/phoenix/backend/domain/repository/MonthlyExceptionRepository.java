package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;
import java.time.LocalDate;
import java.util.List;

// Repositório para acesso a excepções mensais no MongoDB
public interface MonthlyExceptionRepository extends MongoRepository<MonthlyException, String> {
    // Busca excepções por ano-mês
    // Parâm yearMonth: período no formato yyyy-MM
    // Retorna: lista de MonthlyException do mês
    List<MonthlyException> findByYearMonth(LocalDate yearMonth);
    List<MonthlyException> findByYearMonthAndType(LocalDate yearMonth, ExceptionType type);
    List<MonthlyException> findByYearMonthAndMatricula(LocalDate yearMonth, String matricula);
    List<MonthlyException> findByYearMonthAndTypeAndMatricula(LocalDate yearMonth, ExceptionType type, String matricula);
}
