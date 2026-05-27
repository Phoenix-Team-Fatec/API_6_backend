package team.phoenix.backend.commission.application;

import java.util.List;

public interface CommissionAiClient {
    /**
     * Calcula comissões via cliente IA.
     * 
     * @param request dados para cálculo
     * @param year ano de referência
     * @param month mês de referência
     * @param auditoria se true, inclui etapas do cálculo na resposta
     * @return lista de resultados com comissões calculadas
     */
    List<AiCommissionResult> calculate(AiCommissionRequest request, int year, int month, boolean auditoria);
}
