package team.phoenix.backend.service;

import java.time.YearMonth;

// Interface que orquestra o cálculo de comissões buscando dados do BD
public interface CommissionService {

    /**
     * Simula cálculo de comissão para um funcionário em um mês
     * @param matricula matrícula do funcionário (String)
     * @param month mês desejado (YearMonth)
     * @return CommissionResult com dados completos do cálculo
     * @throws RuntimeException se HR ou taxa não encontrados
     */
    CommissionResult simulate(String matricula, YearMonth month);
}
