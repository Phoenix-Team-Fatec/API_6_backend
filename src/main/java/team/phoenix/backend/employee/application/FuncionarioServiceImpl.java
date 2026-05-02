package team.phoenix.backend.employee.application;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team.phoenix.backend.domain.model.Funcionario;
import team.phoenix.backend.domain.model.HrRecord;
import team.phoenix.backend.domain.repository.FuncionarioRepository;
import team.phoenix.backend.domain.repository.HrRecordRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FuncionarioServiceImpl implements FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final HrRecordRepository hrRecordRepository;

    /**
     * Lista todos os funcionários ativos sem repetição
     */
    @Override
    public List<Funcionario> listActive() {
        return funcionarioRepository.findByAtivoTrue();
    }

    /**
     * Busca um funcionário ativo por matrícula
     */
    @Override
    public Optional<Funcionario> findByMatricula(String matricula) {
        return funcionarioRepository.findByMatriculaAndAtivoTrue(matricula);
    }

    /**
     * Soft delete: marca um funcionário como deletado
     */
    @Override
    public void softDelete(String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Funcionário não encontrado");
        }

        var funcionario = funcionarioRepository.findById(objectId)
            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

        if (funcionario.isAtivo()) {
            funcionario.setAtivo(false);
            funcionario.setDeletedAt(new Date());
            funcionarioRepository.save(funcionario);
            log.info("Funcionário {} marcado como deletado", id);
        }
    }

    /**
     * Reativa um funcionário deletado
     */
    @Override
    public void reactivate(String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Funcionário não encontrado");
        }

        var funcionario = funcionarioRepository.findById(objectId)
            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

        if (!funcionario.isAtivo()) {
            funcionario.setAtivo(true);
            funcionario.setDeletedAt(null);
            funcionarioRepository.save(funcionario);
            log.info("Funcionário {} reativado", id);
        }
    }

    /**
     * Consolida dados de hr_records em funcionarios
     * Remove duplicatas pegando o registro mais recente de cada matrícula
     */
    @Override
    public void consolidateFromHrRecords() {
        log.info("Iniciando consolidação de hr_records para funcionarios");
        
        // Busca todos os registros únicos (sem duplicatas) de hr_records
        List<HrRecord> latestHrRecords = hrRecordRepository.findLatestByEachMatricula();
        
        log.info("Encontrados {} funcionários únicos em hr_records", latestHrRecords.size());
        
        // Para cada HrRecord, verifica se funcionário existe; caso contrário, cria novo
        List<Funcionario> funcionariosConsolidados = latestHrRecords.stream()
            .map(hrRecord -> {
                Optional<Funcionario> existente = funcionarioRepository.findByMatricula(hrRecord.getMatricula());
                
                Funcionario funcionario;
                if (existente.isPresent()) {
                    // Atualiza funcionário existente com dados mais recentes
                    funcionario = existente.get();
                    log.debug("Atualizando funcionário existente: {}", hrRecord.getMatricula());
                } else {
                    // Cria novo funcionário
                    funcionario = new Funcionario();
                    funcionario.setId(new ObjectId());
                    funcionario.setCriadoEm(new Date());
                    log.debug("Criando novo funcionário: {}", hrRecord.getMatricula());
                }
                
                // Consolida dados do HrRecord
                funcionario.setMatricula(hrRecord.getMatricula());
                funcionario.setDataRef(hrRecord.getDataRef());
                funcionario.setCodMarca(hrRecord.getCodMarca());
                funcionario.setDescrMarca(hrRecord.getDescrMarca());
                funcionario.setCodLoja(hrRecord.getCodLoja());
                funcionario.setDescrLoja(hrRecord.getDescrLoja());
                funcionario.setDataAdmiss(hrRecord.getDataAdmiss());
                funcionario.setDataDemiss(hrRecord.getDataDemiss());
                funcionario.setCodCargo(hrRecord.getCodCargo());
                funcionario.setDescriCargo(hrRecord.getDescriCargo());
                funcionario.setAtivo(true);
                funcionario.setAtualizadoEm(new Date());
                
                return funcionario;
            })
            .collect(Collectors.toList());
        
        // Salva todos os funcionários consolidados
        funcionarioRepository.saveAll(funcionariosConsolidados);
        log.info("Consolidação concluída: {} funcionários salvos", funcionariosConsolidados.size());
    }
}