package team.phoenix.backend.service;

import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;
import java.util.List;

// Interface que expõe listagem de regras de comissão e exceções mensais
public interface RulesService {

    /**
     * Lista taxas de comissão com filtros opcionais por marca e cargo
     * @param codMarca código da marca (opcional)
     * @param codCargo código do cargo (opcional)
     * @return lista de CommissionRate filtrada
     */
    List<CommissionRate> listRates(Integer codMarca, Integer codCargo);

    /**
     * Lista todas as exceções mensais de todos os meses
     * @return lista completa de MonthlyException
     */
    List<MonthlyException> listAllExceptions();

    /**
     * Lista exceções mensais de um mês com filtros opcionais
     * @param yearMonth período no formato yyyy-MM
     * @param type tipo de exceção (opcional)
     * @param matricula matrícula do funcionário (opcional)
     * @return lista de MonthlyException filtrada
     */
    List<MonthlyException> listExceptions(String yearMonth, ExceptionType type, String matricula);
}
