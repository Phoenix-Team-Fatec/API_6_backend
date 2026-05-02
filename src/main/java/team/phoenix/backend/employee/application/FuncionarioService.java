package team.phoenix.backend.employee.application;

import java.util.List;
import java.util.Optional;

import team.phoenix.backend.domain.model.Funcionario;

public interface FuncionarioService {
    
    /**
     * Lista todos os funcionários ativos sem repetição
     * @return Lista de funcionários únicos e ativos
     */
    List<Funcionario> listActive();

    /**
     * Busca um funcionário ativo por matrícula
     * @param matricula Matrícula do funcionário
     * @return Funcionário com dados consolidados mais recentes
     */
    Optional<Funcionario> findByMatricula(String matricula);

    /**
     * Soft delete: marca um funcionário como deletado
     * @param id ID do funcionário
     * @throws IllegalArgumentException se não encontrado
     */
    void softDelete(String id);

    /**
     * Reativa um funcionário deletado
     * @param id ID do funcionário
     * @throws IllegalArgumentException se não encontrado
     */
    void reactivate(String id);

    /**
     * Consolida dados de hr_records em funcionarios
     * Remove duplicatas e cria registro único de cada funcionário
     */
    void consolidateFromHrRecords();
}