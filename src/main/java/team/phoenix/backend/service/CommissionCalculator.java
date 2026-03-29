package team.phoenix.backend.service;

import team.phoenix.backend.domain.model.HrRecord;
import team.phoenix.backend.domain.model.MonthlyException;
import java.time.YearMonth;
import java.util.List;

// Interface para cálculo complexo de comissões com regras de admissão, demissão, afastamento e férias
public interface CommissionCalculator {

    /**
     * Calcula comissão para um funcionário em um mês
     * @param hr registro de RH do funcionário
     * @param individualSales total de vendas individuais
     * @param storeSales total de vendas da loja
     * @param baseRate taxa de comissão base
     * @param exceptions excepções do mês (afastamentos, férias, bônus, etc)
     * @param month mês do cálculo
     * @return CommissionResult com cálculo completo e explanação
     */
    CommissionResult calculate(HrRecord hr, double individualSales, double storeSales,
                               double baseRate, List<MonthlyException> exceptions,
                               YearMonth month);
}
