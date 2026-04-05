package team.phoenix.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;
import team.phoenix.backend.domain.repository.CommissionRateRepository;
import team.phoenix.backend.domain.repository.MonthlyExceptionRepository;

@Service
@RequiredArgsConstructor
public class RulesServiceImpl implements RulesService {

    private final CommissionRateRepository rateRepo;
    private final MonthlyExceptionRepository exceptionRepo;

    @Override
    public List<CommissionRate> listRates(Integer codMarca, Integer codCargo) {
        return listRatesWithOptions(codMarca, codCargo, false);
    }

    @Override
    public List<CommissionRate> listRatesWithOptions(Integer codMarca, Integer codCargo, boolean includeInactive) {
        List<CommissionRate> rates;

        if (codMarca != null && codCargo != null) {
            rates = rateRepo.findByCodMarcaAndCodCargo(codMarca, codCargo)
                .map(List::of).orElse(List.of());
        } else if (codMarca != null) {
            rates = rateRepo.findByCodMarca(codMarca);
        } else if (codCargo != null) {
            rates = rateRepo.findByCodCargo(codCargo);
        } else {
            rates = rateRepo.findAll();
        }

        // Filtrar por vigência se não incluir inativos
        if (!includeInactive) {
            rates = rates.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsVigente()) && r.getDeletedAt() == null)
                .toList();
        }

        return rates;
    }

    @Override
    public CommissionRate createRate(CommissionRate rule) {
        // Preencher valores padrão
        if (rule.getVersao() == null || rule.getVersao() == 0) {
            rule.setVersao(1);
        }
        if (rule.getIsVigente() == null) {
            rule.setIsVigente(true);
        }
        if (rule.getCreatedAt() == null) {
            rule.setCreatedAt(LocalDateTime.now());
        }
        if (rule.getVersoesAnteriores() == null) {
            rule.setVersoesAnteriores(new ArrayList<>());
        }

        // Gerar representação textual (requisito #10)
        if (rule.getTextoOriginal() == null || rule.getTextoOriginal().isBlank()) {
            rule.setTextoOriginal(TextualRuleGenerator.generate(rule));
        }

        // Gerar pseudocódigo (requisito #11)
        if (rule.getExplicacao() == null || rule.getExplicacao().isBlank()) {
            rule.setExplicacao(PseudoCodeGenerator.generate(rule));
        }

        return rateRepo.save(rule);
    }

    @Override
    public CommissionRate updateRate(String id, CommissionRate updatedRule) {
        Optional<CommissionRate> existing = rateRepo.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Regra não encontrada: " + id);
        }

        CommissionRate current = existing.get();

        // Guardar versão anterior no histórico
        CommissionRate.CommissionRateVersion previousVersion = CommissionRate.CommissionRateVersion.builder()
            .versao(current.getVersao())
            .pctComiss(current.getPctComiss())
            .textoOriginal(current.getTextoOriginal())
            .explicacao(current.getExplicacao())
            .createdAt(current.getCreatedAt())
            .updatedAt(current.getUpdatedAt())
            .isVigente(current.getIsVigente())
            .build();

        current.getVersoesAnteriores().add(previousVersion);

        // Atualizar campos
        current.setPctComiss(updatedRule.getPctComiss());
        current.setData(updatedRule.getData());
        current.setDescrMarca(updatedRule.getDescrMarca());
        current.setDescriCargo(updatedRule.getDescriCargo());
        current.setVersao(current.getVersao() + 1);
        current.setUpdatedAt(LocalDateTime.now());
        Boolean vigente = updatedRule.getIsVigente();
        current.setIsVigente(vigente != null && vigente);

        // Regenerar texto e pseudocódigo
        current.setTextoOriginal(TextualRuleGenerator.generate(current));
        current.setExplicacao(PseudoCodeGenerator.generate(current));

        return rateRepo.save(current);
    }

    @Override
    public void deactivateRate(String id) {
        Optional<CommissionRate> rate = rateRepo.findById(id);
        if (rate.isPresent()) {
            CommissionRate r = rate.get();
            r.setIsVigente(false);
            r.setDeletedAt(LocalDateTime.now());
            rateRepo.save(r);
        } else {
            throw new IllegalArgumentException("Regra não encontrada: " + id);
        }
    }

    @Override
    public void activateRate(String id) {
        Optional<CommissionRate> rate = rateRepo.findById(id);
        if (rate.isEmpty()) {
            throw new IllegalArgumentException("Regra não encontrada: " + id);
        }

        CommissionRate r = rate.get();
        if (r.getDeletedAt() != null) {
            throw new IllegalStateException("Regra removida não pode ser reativada");
        }

        r.setIsVigente(true);
        rateRepo.save(r);
    }

    @Override
    public Optional<CommissionRate> getRateById(String id) {
        return rateRepo.findById(id);
    }

    @Override
    public List<MonthlyException> listExceptions(LocalDate yearMonth, ExceptionType type, String matricula) {
        if (type != null && matricula != null)
            return exceptionRepo.findByYearMonthAndTypeAndMatricula(yearMonth, type, matricula);
        if (type != null) return exceptionRepo.findByYearMonthAndType(yearMonth, type);
        if (matricula != null) return exceptionRepo.findByYearMonthAndMatricula(yearMonth, matricula);
        return exceptionRepo.findByYearMonth(yearMonth);
    }
}
