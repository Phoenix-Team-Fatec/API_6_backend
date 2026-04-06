package team.phoenix.backend.service;

import java.time.LocalDate;
import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;
import java.util.List;
import java.util.Optional;

import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;

// Interface que expõe listagem de regras de comissão e exceções mensais
public interface RulesService {

     /**
      * Lista as taxas de comissão com filtros opcionais (somente não deletadas)
      * @param codMarca código da marca (opcional)
      * @param codCargo código do cargo (opcional)
      * @param isVigente filtro opcional de vigência; null retorna ativas e inativas
      * @return lista de taxas de comissão não deletadas
      */
     List<CommissionRate> listRates(Integer codMarca, Integer codCargo, Boolean isVigente);

    /**
     * Lista as taxas de comissão na lixeira (somente deletadas)
     * @param codMarca código da marca (opcional)
     * @param codCargo código do cargo (opcional)
     * @param isVigente filtro opcional de vigência
     * @return lista de taxas de comissão deletadas
     */
    List<CommissionRate> listTrashedRates(Integer codMarca, Integer codCargo, Boolean isVigente);

    /**
     * Lista as taxas de comissão incluindo versões anteriores
     * @param codMarca código da marca (opcional)
     * @param codCargo código do cargo (opcional)
     * @param includeInactive se true, inclui regras inativas
     * @return lista de taxas de comissão
     * Lista taxas de comissão com filtros opcionais por marca e cargo
     * @param codMarca código da marca (opcional)
     * @param codCargo código do cargo (opcional)
     * @return lista de CommissionRate filtrada
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
        * Desativa uma regra, sem excluí-la
     * @param id ID da regra
     */
    void deactivateRate(String id);

        /**
        * Exclui uma regra via soft delete
        * @param id ID da regra
        */
        void softDeleteRate(String id);

    /**
     * Ativa uma regra existente, desde que não tenha sido removida (soft delete)
     * @param id ID da regra
     */
    void activateRate(String id);

    /**
     * Restaura uma regra removida (soft delete), mantendo-a inativa
     * @param id ID da regra
     */
    void restoreRate(String id);

    /**
     * Reverte a regra para a versão anterior (ex.: 5 para 4)
     * Se não houver histórico anterior, não realiza alteração.
     * @param id ID da regra
     */
    void rollbackRate(String id);

    /**
     * Obtém uma regra pelo ID
     * @param id ID da regra
     * @return opcional contendo a regra
     */
    Optional<CommissionRate> getRateById(String id);

    /**
     * Lista as exceções mensais com filtros opcionais
    * @param yearMonth dia 1 do mês de referência
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
    List<MonthlyException> listExceptions(LocalDate yearMonth, ExceptionType type, String matricula);
}
