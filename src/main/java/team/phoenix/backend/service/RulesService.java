package team.phoenix.backend.service;

import team.phoenix.backend.domain.model.*;
import java.util.List;

// Interface para gerenciamento de taxas de comissão e exceções mensais
public interface RulesService {

    /**
     * Lista as taxas de comissão com filtros opcionais
     * @param codMarca código da marca (opcional)
     * @param codCargo código do cargo (opcional)
     * @return lista de taxas de comissão
     */
    List<CommissionRate> listRates(Integer codMarca, Integer codCargo);

    /**
     * Lista as exceções mensais com filtros opcionais
     * @param yearMonth ano e mês no formato yyyy-MM
     * @param type tipo de exceção (opcional)
     * @param matricula matrícula do funcionário (opcional)
     * @return lista de exceções mensais
     */
    List<MonthlyException> listExceptions(String yearMonth, ExceptionType type, String matricula);
}
