package team.phoenix.backend.service;

import java.time.LocalDate;

// Interface que orquestra o cálculo de comissões buscando dados do BD
public interface CommissionService {

    /**
     * Simula cálculo de comissão para um funcionário em um mês
     * @param matricula matrícula do funcionário (String)
    * @param month mês desejado (dia 1 do mês)
     * @return CommissionResult com dados completos do cálculo
     * @throws RuntimeException se HR ou taxa não encontrados
     */
    CommissionResult simulate(String matricula, LocalDate month);

    /**
     * Calcula comissao por funcionario, loja ou marca em um mes.
     */
    CommissionCalculationResult calculate(CommissionCalculationCommand command);
}
