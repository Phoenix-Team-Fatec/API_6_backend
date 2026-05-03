package team.phoenix.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.audit.application.AuditLogService;
import team.phoenix.backend.audit.domain.AuditAction;
import team.phoenix.backend.audit.domain.AuditLogEntry;
import team.phoenix.backend.audit.domain.AuditResourceType;
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
    private final AuditLogService auditLogService;

    @Override
    public List<CommissionRate> listRates(Integer codMarca, Integer codCargo, Boolean isVigente) {
        return listRatesWithOptions(codMarca, codCargo, isVigente);
    }

    @Override
    public List<CommissionRate> listTrashedRates(Integer codMarca, Integer codCargo, Boolean isVigente) {
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

        var deletedRates = rates.stream()
            .filter(r -> r.getDeletedAt() != null)
            .toList();

        if (isVigente == null) {
            return deletedRates;
        }

        return deletedRates.stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsVigente()) == isVigente)
            .toList();
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

    private List<CommissionRate> listRatesWithOptions(Integer codMarca, Integer codCargo, Boolean isVigente) {
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

        var notDeletedRates = rates.stream()
            .filter(r -> r.getDeletedAt() == null)
            .toList();

        if (isVigente == null) {
            return notDeletedRates;
        }

        return notDeletedRates.stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsVigente()) == isVigente)
            .toList();
    }

    @Override
    public CommissionRate createRate(CommissionRate rule) {
        validateCreateRate(rule);

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

        CommissionRate saved = rateRepo.save(rule);
        recordRateAudit(AuditAction.CREATE, saved != null ? saved : rule, "Regra de comissao criada");
        return saved;
    }

    @Override
    public CommissionRate updateRate(String id, CommissionRate updatedRule) {
        validatePartialRateUpdate(updatedRule);

        Optional<CommissionRate> existing = rateRepo.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Regra não encontrada: " + id);
        }

        CommissionRate current = existing.get();

        if (current.getVersoesAnteriores() == null) {
            current.setVersoesAnteriores(new ArrayList<>());
        }

        // Guardar versão anterior no histórico, sem carregar versoesAnteriores
        CommissionRate.CommissionRateVersion previousVersion = CommissionRate.CommissionRateVersion.builder()
            .codMarca(current.getCodMarca())
            .descrMarca(current.getDescrMarca())
            .codCargo(current.getCodCargo())
            .descriCargo(current.getDescriCargo())
            .pctComiss(current.getPctComiss())
            .data(current.getData())
            .versao(current.getVersao())
            .textoOriginal(current.getTextoOriginal())
            .explicacao(current.getExplicacao())
            .createdAt(current.getCreatedAt())
            .updatedAt(current.getUpdatedAt())
            .isVigente(current.getIsVigente())
            .deletedAt(current.getDeletedAt())
            .build();

        current.getVersoesAnteriores().add(previousVersion);

        // Atualizar somente os campos enviados; manter o restante
        if (updatedRule.getCodMarca() != null) {
            current.setCodMarca(updatedRule.getCodMarca());
        }
        if (updatedRule.getDescrMarca() != null) {
            current.setDescrMarca(updatedRule.getDescrMarca());
        }
        if (updatedRule.getCodCargo() != null) {
            current.setCodCargo(updatedRule.getCodCargo());
        }
        if (updatedRule.getDescriCargo() != null) {
            current.setDescriCargo(updatedRule.getDescriCargo());
        }
        if (updatedRule.getPctComiss() != null) {
            current.setPctComiss(updatedRule.getPctComiss());
        }
        if (updatedRule.getData() != null) {
            current.setData(updatedRule.getData());
        }
        if (updatedRule.getTextoOriginal() != null) {
            current.setTextoOriginal(updatedRule.getTextoOriginal());
        }
        if (updatedRule.getExplicacao() != null) {
            current.setExplicacao(updatedRule.getExplicacao());
        }
        if (updatedRule.getIsVigente() != null) {
            current.setIsVigente(updatedRule.getIsVigente());
        }
        current.setVersao(current.getVersao() + 1);
        current.setUpdatedAt(LocalDateTime.now());

        // Atualizar texto e pseudocódigo apenas quando forem enviados
        if (updatedRule.getTextoOriginal() != null) {
            current.setTextoOriginal(updatedRule.getTextoOriginal());
        }
        if (updatedRule.getExplicacao() != null) {
            current.setExplicacao(updatedRule.getExplicacao());
        }

        CommissionRate saved = rateRepo.save(current);
        recordRateAudit(AuditAction.UPDATE, current, "Regra de comissao atualizada");
        return saved;
    }

    @Override
    public void deactivateRate(String id) {
        Optional<CommissionRate> rate = rateRepo.findById(id);
        if (rate.isPresent()) {
            CommissionRate r = rate.get();
            r.setIsVigente(false);
            rateRepo.save(r);
            recordRateAudit(AuditAction.DEACTIVATE, r, "Regra de comissao desativada");
        } else {
            throw new IllegalArgumentException("Regra não encontrada: " + id);
        }
    }

    @Override
    public void softDeleteRate(String id) {
        Optional<CommissionRate> rate = rateRepo.findById(id);
        if (rate.isPresent()) {
            CommissionRate r = rate.get();
            r.setIsVigente(false);
            r.setDeletedAt(LocalDateTime.now());
            rateRepo.save(r);
            recordRateAudit(AuditAction.SOFT_DELETE, r, "Regra de comissao removida");
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
        recordRateAudit(AuditAction.ACTIVATE, r, "Regra de comissao ativada");
    }

    @Override
    public void restoreRate(String id) {
        Optional<CommissionRate> rate = rateRepo.findById(id);
        if (rate.isEmpty()) {
            throw new IllegalArgumentException("Regra não encontrada: " + id);
        }

        CommissionRate r = rate.get();
        if (r.getDeletedAt() == null) {
            throw new IllegalStateException("Regra não foi deletada");
        }

        r.setDeletedAt(null);
        rateRepo.save(r);
        recordRateAudit(AuditAction.RESTORE, r, "Regra de comissao restaurada");
    }

    @Override
    public void rollbackRate(String id) {
        Optional<CommissionRate> rate = rateRepo.findById(id);
        if (rate.isEmpty()) {
            throw new IllegalArgumentException("Regra não encontrada: " + id);
        }

        CommissionRate current = rate.get();
        if (current.getVersoesAnteriores() == null || current.getVersoesAnteriores().isEmpty() ||
            (current.getVersao() != null && current.getVersao() <= 1)) {
            return;
        }

        int lastIndex = current.getVersoesAnteriores().size() - 1;
        CommissionRate.CommissionRateVersion previous = current.getVersoesAnteriores().remove(lastIndex);

        current.setCodMarca(previous.getCodMarca());
        current.setDescrMarca(previous.getDescrMarca());
        current.setCodCargo(previous.getCodCargo());
        current.setDescriCargo(previous.getDescriCargo());
        current.setPctComiss(previous.getPctComiss());
        current.setData(previous.getData());
        current.setTextoOriginal(previous.getTextoOriginal());
        current.setExplicacao(previous.getExplicacao());
        current.setVersao(previous.getVersao());
        current.setIsVigente(previous.getIsVigente());
        current.setCreatedAt(previous.getCreatedAt());
        current.setUpdatedAt(previous.getUpdatedAt());
        current.setDeletedAt(previous.getDeletedAt());

        rateRepo.save(current);
        recordRateAudit(AuditAction.ROLLBACK, current, "Regra de comissao revertida");
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

    private void validateCreateRate(CommissionRate rule) {
        if (rule == null) {
            throw new IllegalArgumentException("request body is required");
        }
        if (rule.getCodMarca() == null) {
            throw new IllegalArgumentException("codMarca is required");
        }
        if (rule.getCodCargo() == null) {
            throw new IllegalArgumentException("codCargo is required");
        }
        if (rule.getPctComiss() == null || rule.getPctComiss() < 0) {
            throw new IllegalArgumentException("pctComiss must be greater than or equal to zero");
        }
        if (rule.getDescrMarca() == null || rule.getDescrMarca().isBlank()) {
            throw new IllegalArgumentException("descrMarca is required");
        }
        if (rule.getDescriCargo() == null || rule.getDescriCargo().isBlank()) {
            throw new IllegalArgumentException("descriCargo is required");
        }
    }

    private void validatePartialRateUpdate(CommissionRate rule) {
        if (rule == null) {
            throw new IllegalArgumentException("request body is required");
        }
        if (rule.getPctComiss() != null && rule.getPctComiss() < 0) {
            throw new IllegalArgumentException("pctComiss must be greater than or equal to zero");
        }
        if (rule.getDescrMarca() != null && rule.getDescrMarca().isBlank()) {
            throw new IllegalArgumentException("descrMarca is required");
        }
        if (rule.getDescriCargo() != null && rule.getDescriCargo().isBlank()) {
            throw new IllegalArgumentException("descriCargo is required");
        }
    }

    private void recordRateAudit(AuditAction action, CommissionRate rate, String summary) {
        auditLogService.record(new AuditLogEntry(
            action,
            AuditResourceType.COMMISSION_RATE,
            rate.getId(),
            summary,
            rateMetadata(rate)
        ));
    }

    private Map<String, Object> rateMetadata(CommissionRate rate) {
        var metadata = new HashMap<String, Object>();
        putIfPresent(metadata, "codMarca", rate.getCodMarca());
        putIfPresent(metadata, "codCargo", rate.getCodCargo());
        putIfPresent(metadata, "versao", rate.getVersao());
        putIfPresent(metadata, "pctComiss", rate.getPctComiss());
        putIfPresent(metadata, "isVigente", rate.getIsVigente());
        putIfPresent(metadata, "deletedAt", rate.getDeletedAt());
        return metadata;
    }

    private void putIfPresent(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }
}
