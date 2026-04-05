package team.phoenix.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;

// Interface para gerenciamento de taxas de comissão e exceções mensais
public interface RulesService {

    /**
     * Lista as taxas de comissão com filtros opcionais (apenas vigentes por padrão)
     * @param codMarca código da marca (opcional)
     * @param codCargo código do cargo (opcional)
     * @return lista de taxas de comissão vigentes
     */
    List<CommissionRate> listRates(Integer codMarca, Integer codCargo);

    /**
     * Lista as taxas de comissão incluindo versões anteriores
     * @param codMarca código da marca (opcional)
     * @param codCargo código do cargo (opcional)
     * @param includeInactive se true, inclui regras inativas
     * @return lista de taxas de comissão
     */
    List<CommissionRate> listRatesWithOptions(Integer codMarca, Integer codCargo, boolean includeInactive);

    /**
     * Cria uma nova regra de comissão com geração automática de texto e pseudocódigo
     * @param rule regra a ser criada
     * @return regra criada
     */
    CommissionRate createRate(CommissionRate rule);

    /**
     * Atualiza uma regra existente, criando uma nova versão
     * @param id ID da regra
     * @param updatedRule dados atualizados
     * @return regra atualizada
     */
    CommissionRate updateRate(String id, CommissionRate updatedRule);

    /**
     * Desativa uma regra (soft delete)
     * @param id ID da regra
     */
    void deactivateRate(String id);

    /**
     * Ativa uma regra existente, desde que não tenha sido removida (soft delete)
     * @param id ID da regra
     */
    void activateRate(String id);

    /**
     * Obtém uma regra pelo ID
     * @param id ID da regra
     * @return opcional contendo a regra
     */
    Optional<CommissionRate> getRateById(String id);

    /**
     * Lista as exceções mensais com filtros opcionais
    * @param yearMonth dia 1 do mês de referência
     * @param type tipo de exceção (opcional)
     * @param matricula matrícula do funcionário (opcional)
     * @return lista de exceções mensais
     */
    List<MonthlyException> listExceptions(LocalDate yearMonth, ExceptionType type, String matricula);
}
