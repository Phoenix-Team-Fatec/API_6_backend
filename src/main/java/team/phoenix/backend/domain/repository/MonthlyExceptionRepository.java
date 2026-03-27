package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;
import java.util.List;

public interface MonthlyExceptionRepository extends MongoRepository<MonthlyException, String> {
    List<MonthlyException> findByYearMonth(String yearMonth);
    List<MonthlyException> findByYearMonthAndType(String yearMonth, ExceptionType type);
    List<MonthlyException> findByYearMonthAndMatricula(String yearMonth, String matricula);
    List<MonthlyException> findByYearMonthAndTypeAndMatricula(String yearMonth, ExceptionType type, String matricula);
}
