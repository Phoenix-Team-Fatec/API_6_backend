package team.phoenix.backend.rules.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.phoenix.backend.ai.application.AiAgentIntercorrencia;
import team.phoenix.backend.ai.application.AiAgentOverride;
import team.phoenix.backend.ai.application.AiAgentResponse;
import team.phoenix.backend.ai.application.AiIntegrationClient;
import team.phoenix.backend.audit.application.AuditLogService;
import team.phoenix.backend.audit.domain.AuditAction;
import team.phoenix.backend.audit.domain.AuditResourceType;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RulesServiceTest {

    @Mock CommissionRateRepository rateRepo;
    @Mock MonthlyExceptionRepository exceptionRepo;
    @Mock BrandRepository brandRepository;
    @Mock PositionRepository positionRepository;
    @Mock AiIntegrationClient aiIntegrationClient;
    @Mock AuditLogService auditLogService;
    @InjectMocks RulesServiceImpl service;

    @Test void createRate_rejectsMissingCodMarca() {
        var rate = validRate();
        rate.setCodMarca(null);

        assertThatThrownBy(() -> service.createRate(rate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("codMarca is required");
        verify(rateRepo, never()).save(any());
    }

    @Test void createRate_rejectsMissingCodCargo() {
        var rate = validRate();
        rate.setCodCargo(null);

        assertThatThrownBy(() -> service.createRate(rate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("codCargo is required");
        verify(rateRepo, never()).save(any());
    }

    @Test void createRate_rejectsNegativePctComiss() {
        var rate = validRate();
        rate.setPctComiss(-0.01);

        assertThatThrownBy(() -> service.createRate(rate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("pctComiss must be greater than or equal to zero");
        verify(rateRepo, never()).save(any());
    }

    @Test void createRate_rejectsBlankDescriptions() {
        var missingBrandDescription = validRate();
        missingBrandDescription.setDescrMarca(" ");
        assertThatThrownBy(() -> service.createRate(missingBrandDescription))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("descrMarca is required");

        var missingCargoDescription = validRate();
        missingCargoDescription.setDescriCargo("");
        assertThatThrownBy(() -> service.createRate(missingCargoDescription))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("descriCargo is required");

        verify(rateRepo, never()).save(any());
    }

    @Test void listRates_noFilter_returnsActiveAndInactiveButNotDeleted() {
        var active = CommissionRate.builder().isVigente(true).deletedAt(null).build();
        var inactive = CommissionRate.builder().isVigente(false).deletedAt(null).build();
        var deletedInactive = CommissionRate.builder().isVigente(false).deletedAt(LocalDateTime.now()).build();
        when(rateRepo.findAll()).thenReturn(List.of(active, inactive, deletedInactive));

        assertThat(service.listRates(null, null, null)).containsExactly(active, inactive);
    }

    @Test void listRates_filterByCodMarca_returnsFiltered() {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).isVigente(true).build();
        when(rateRepo.findByCodMarca(10)).thenReturn(List.of(rate));
        assertThat(service.listRates(10, null, null)).hasSize(1);
        verify(rateRepo).findByCodMarca(10);
    }

    @Test void listRates_filterByCodCargo_returnsFiltered() {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).isVigente(true).build();
        when(rateRepo.findByCodCargo(100)).thenReturn(List.of(rate));
        assertThat(service.listRates(null, 100, null)).hasSize(1);
        verify(rateRepo).findByCodCargo(100);
    }

    @Test void listRates_filterByBoth_returnsFiltered() {
        var rate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).isVigente(true).build();
        when(rateRepo.findByCodMarcaAndCodCargo(10, 100)).thenReturn(List.of(rate));
        assertThat(service.listRates(10, 100, null)).hasSize(1);
        verify(rateRepo).findByCodMarcaAndCodCargo(10, 100);
    }

    @Test void listRates_filterByBoth_returnsMultipleRulesForSameBrandAndPosition() {
        var oldRate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.025).isVigente(true).build();
        var generatedRate = CommissionRate.builder().codMarca(10).codCargo(100).pctComiss(0.06).isVigente(true).build();
        when(rateRepo.findByCodMarcaAndCodCargo(10, 100)).thenReturn(List.of(oldRate, generatedRate));

        assertThat(service.listRates(10, 100, null)).containsExactly(oldRate, generatedRate);
    }

    @Test void listRates_withIsVigenteTrue_returnsOnlyActive() {
        var active = CommissionRate.builder().isVigente(true).build();
        var inactive = CommissionRate.builder().isVigente(false).build();
        when(rateRepo.findAll()).thenReturn(List.of(active, inactive));

        assertThat(service.listRates(null, null, true)).containsExactly(active);
    }

    @Test void listRates_withIsVigenteFalse_returnsOnlyInactive() {
        var active = CommissionRate.builder().isVigente(true).build();
        var inactive = CommissionRate.builder().isVigente(false).build();
        when(rateRepo.findAll()).thenReturn(List.of(active, inactive));

        assertThat(service.listRates(null, null, false)).containsExactly(inactive);
    }

    @Test void listRates_withIsVigenteFalse_excludesSoftDeleted() {
        var inactive = CommissionRate.builder()
            .isVigente(false)
            .deletedAt(null)
            .build();
        var deletedInactive = CommissionRate.builder()
            .isVigente(false)
            .deletedAt(LocalDateTime.now())
            .build();
        when(rateRepo.findAll()).thenReturn(List.of(inactive, deletedInactive));

        assertThat(service.listRates(null, null, false)).containsExactly(inactive);
    }

    @Test void listTrashedRates_noFilter_returnsOnlyDeleted() {
        var deletedA = CommissionRate.builder().isVigente(false).deletedAt(LocalDateTime.now()).build();
        var active = CommissionRate.builder().isVigente(true).deletedAt(null).build();
        when(rateRepo.findAll()).thenReturn(List.of(deletedA, active));

        assertThat(service.listTrashedRates(null, null, null)).containsExactly(deletedA);
    }

    @Test void listTrashedRates_withIsVigenteFalse_filtersDeletedInactive() {
        var deletedInactive = CommissionRate.builder().isVigente(false).deletedAt(LocalDateTime.now()).build();
        var deletedActive = CommissionRate.builder().isVigente(true).deletedAt(LocalDateTime.now()).build();
        when(rateRepo.findAll()).thenReturn(List.of(deletedInactive, deletedActive));

        assertThat(service.listTrashedRates(null, null, false)).containsExactly(deletedInactive);
    }

    @Test void listExceptions_byMonth_returnsAll() {
        var ex = MonthlyException.builder().yearMonth(LocalDate.of(2025,7,1)).type(ExceptionType.ABSENCE).build();
        when(exceptionRepo.findByYearMonth(LocalDate.of(2025,7,1))).thenReturn(List.of(ex));
        assertThat(service.listExceptions(LocalDate.of(2025,7,1), null, null)).hasSize(1);
    }

    @Test void listExceptions_byMonthAndType_returnsFiltered() {
        var ex = MonthlyException.builder().yearMonth(LocalDate.of(2025,7,1)).type(ExceptionType.ABSENCE).build();
        when(exceptionRepo.findByYearMonthAndType(LocalDate.of(2025,7,1), ExceptionType.ABSENCE)).thenReturn(List.of(ex));
        assertThat(service.listExceptions(LocalDate.of(2025,7,1), ExceptionType.ABSENCE, null)).hasSize(1);
    }

    @Test void listExceptions_byMonthAndMatricula_returnsFiltered() {
        var ex = MonthlyException.builder().yearMonth(LocalDate.of(2025,7,1)).matricula("MATRIC-58").build();
        when(exceptionRepo.findByYearMonthAndMatricula(LocalDate.of(2025,7,1), "MATRIC-58")).thenReturn(List.of(ex));
        assertThat(service.listExceptions(LocalDate.of(2025,7,1), null, "MATRIC-58")).hasSize(1);
    }

    @Test void generateFromNaturalLanguage_withOverride_createsCommissionRateUsingAiExplanation() {
        var prompt = "Em maio de 2026, marca 10 cargo 100 recebe 5% de comissao";
        var override = new AiAgentOverride(
            "Regra sazonal",
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 31),
            java.util.Map.of("10,100", 5.0),
            java.util.Map.of(),
            java.util.Map.of()
        );
        var aiResponse = new AiAgentResponse("override", override, null, "A IA alterou a taxa para 5% em maio.");
        var saved = CommissionRate.builder()
            .id("rate-1")
            .codMarca(10)
            .descrMarca("PRETO")
            .codCargo(100)
            .descriCargo("VENDEDOR")
            .pctComiss(0.05)
            .data(LocalDate.of(2026, 5, 1))
            .textoOriginal(prompt)
            .explicacao("A IA alterou a taxa para 5% em maio.")
            .versao(1)
            .isVigente(true)
            .build();

        when(aiIntegrationClient.askAgent(prompt)).thenReturn(aiResponse);
        when(brandRepository.findByCodigo(10)).thenReturn(Optional.of(Brand.builder().codigo(10).nome("PRETO").build()));
        when(positionRepository.findByCodigo(100)).thenReturn(Optional.of(Position.builder().codigo(100).nome("VENDEDOR").build()));
        when(rateRepo.save(any())).thenReturn(saved);

        var result = service.generateFromNaturalLanguage(prompt);

        assertThat(result.tipo()).isEqualTo("override");
        assertThat(result.rules()).containsExactly(saved);
        assertThat(result.exceptions()).isEmpty();
        verify(rateRepo).save(argThat(rate ->
            rate.getCodMarca().equals(10)
                && rate.getCodCargo().equals(100)
                && rate.getPctComiss().equals(0.05)
                && rate.getTextoOriginal().equals(prompt)
                && rate.getExplicacao().equals("A IA alterou a taxa para 5% em maio.")
        ));
    }

    @Test void generateFromNaturalLanguage_withIntercorrencia_createsMonthlyException() {
        var prompt = "MATRIC-1 recebe bonus fixo de 500 reais em maio";
        var aiException = new AiAgentIntercorrencia(
            "MATRIC-1",
            "bonus_fixo",
            500.0,
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 31)
        );
        var aiResponse = new AiAgentResponse(
            "intercorrencia",
            null,
            List.of(aiException),
            "A IA gerou bonus fixo mensal para a matricula."
        );
        var saved = MonthlyException.builder()
            .id("ex-1")
            .yearMonth(LocalDate.of(2026, 5, 1))
            .type(ExceptionType.BONUS_FIXED)
            .matricula("MATRIC-1")
            .amount(500.0)
            .startDate(LocalDate.of(2026, 5, 1))
            .endDate(LocalDate.of(2026, 5, 31))
            .build();

        when(aiIntegrationClient.askAgent(prompt)).thenReturn(aiResponse);
        when(exceptionRepo.save(any())).thenReturn(saved);

        var result = service.generateFromNaturalLanguage(prompt);

        assertThat(result.tipo()).isEqualTo("intercorrencia");
        assertThat(result.rules()).isEmpty();
        assertThat(result.exceptions()).containsExactly(saved);
        verify(exceptionRepo).save(argThat(exception ->
            exception.getType() == ExceptionType.BONUS_FIXED
                && exception.getMatricula().equals("MATRIC-1")
                && exception.getAmount().equals(500.0)
        ));
    }

    @Test void listExceptions_byMonthTypeAndMatricula_returnsFiltered() {
        var ex = MonthlyException.builder().yearMonth(LocalDate.of(2025,7,1)).type(ExceptionType.ABSENCE).matricula("MATRIC-58").build();
        when(exceptionRepo.findByYearMonthAndTypeAndMatricula(LocalDate.of(2025,7,1), ExceptionType.ABSENCE, "MATRIC-58")).thenReturn(List.of(ex));
        assertThat(service.listExceptions(LocalDate.of(2025,7,1), ExceptionType.ABSENCE, "MATRIC-58")).hasSize(1);
        verify(exceptionRepo).findByYearMonthAndTypeAndMatricula(LocalDate.of(2025,7,1), ExceptionType.ABSENCE, "MATRIC-58");
    }

    @Test void updateRate_mergesOnlyProvidedFields_andStoresFullPreviousVersion() {
        var current = CommissionRate.builder()
            .id("123")
            .codMarca(10)
            .descrMarca("PRETO")
            .codCargo(100)
            .descriCargo("VENDEDOR LOJA")
            .pctComiss(0.025)
            .data(LocalDate.of(2026, 3, 1))
            .textoOriginal("texto atual")
            .explicacao("explicacao atual")
            .versao(3)
            .isVigente(true)
            .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
            .updatedAt(LocalDateTime.of(2026, 4, 2, 10, 0))
            .deletedAt(null)
            .versoesAnteriores(new java.util.ArrayList<>())
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(current));

        var updated = CommissionRate.builder()
            .descrMarca("CINZA")
            .pctComiss(0.03)
            .isVigente(false)
            .build();

        service.updateRate("123", updated);

        assertThat(current.getCodMarca()).isEqualTo(10);
        assertThat(current.getDescrMarca()).isEqualTo("CINZA");
        assertThat(current.getCodCargo()).isEqualTo(100);
        assertThat(current.getDescriCargo()).isEqualTo("VENDEDOR LOJA");
        assertThat(current.getPctComiss()).isEqualTo(0.03);
        assertThat(current.getData()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(current.getTextoOriginal()).isEqualTo("texto atual");
        assertThat(current.getExplicacao()).isEqualTo("explicacao atual");
        assertThat(current.getVersao()).isEqualTo(4);
        assertThat(current.getUpdatedAt()).isNotNull();
        assertThat(current.getIsVigente()).isFalse();
        assertThat(current.getVersoesAnteriores()).hasSize(1);

        var previous = current.getVersoesAnteriores().get(0);
        assertThat(previous.getCodMarca()).isEqualTo(10);
        assertThat(previous.getDescrMarca()).isEqualTo("PRETO");
        assertThat(previous.getCodCargo()).isEqualTo(100);
        assertThat(previous.getDescriCargo()).isEqualTo("VENDEDOR LOJA");
        assertThat(previous.getPctComiss()).isEqualTo(0.025);
        assertThat(previous.getData()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(previous.getVersao()).isEqualTo(3);
        assertThat(previous.getTextoOriginal()).isEqualTo("texto atual");
        assertThat(previous.getExplicacao()).isEqualTo("explicacao atual");
        assertThat(previous.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 4, 1, 10, 0));
        assertThat(previous.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 4, 2, 10, 0));
        assertThat(previous.getIsVigente()).isTrue();
        assertThat(previous.getDeletedAt()).isNull();
        verify(rateRepo).save(current);
        verify(auditLogService).record(argThat(entry ->
            entry.action() == AuditAction.UPDATE
                && entry.resourceType() == AuditResourceType.COMMISSION_RATE
                && entry.resourceId().equals("123")
        ));
    }

    @Test void activateRate_whenNotDeleted_reattivatesRule() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(false)
            .deletedAt(null)
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        service.activateRate("123");

        assertThat(rate.getIsVigente()).isTrue();
        verify(rateRepo).save(rate);
    }

    @Test void activateRate_whenDeleted_throwsIllegalStateException() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(false)
            .deletedAt(LocalDateTime.now())
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        assertThatThrownBy(() -> service.activateRate("123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Regra removida não pode ser reativada");
        verify(rateRepo, never()).save(any());
    }

    @Test void deactivateRate_whenFound_marksAsDeletedAndInactive() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(true)
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        service.deactivateRate("123");

        assertThat(rate.getIsVigente()).isFalse();
        assertThat(rate.getDeletedAt()).isNull();
        verify(rateRepo).save(rate);
        verify(auditLogService).record(argThat(entry ->
            entry.action() == AuditAction.DEACTIVATE
                && entry.resourceType() == AuditResourceType.COMMISSION_RATE
                && entry.resourceId().equals("123")
        ));
    }

    @Test void softDeleteRate_whenFound_marksAsDeletedAndInactive() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(true)
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        service.softDeleteRate("123");

        assertThat(rate.getIsVigente()).isFalse();
        assertThat(rate.getDeletedAt()).isNotNull();
        verify(rateRepo).save(rate);
        verify(auditLogService).record(argThat(entry ->
            entry.action() == AuditAction.SOFT_DELETE
                && entry.resourceType() == AuditResourceType.COMMISSION_RATE
                && entry.resourceId().equals("123")
        ));
    }

    @Test void restoreRate_whenDeleted_recoversWithoutActivating() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(false)
            .deletedAt(LocalDateTime.now())
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        service.restoreRate("123");

        assertThat(rate.getDeletedAt()).isNull();
        assertThat(rate.getIsVigente()).isFalse();
        verify(rateRepo).save(rate);
    }

    @Test void restoreRate_whenNotDeleted_throwsIllegalStateException() {
        var rate = CommissionRate.builder()
            .id("123")
            .isVigente(true)
            .deletedAt(null)
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(rate));

        assertThatThrownBy(() -> service.restoreRate("123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Regra não foi deletada");
        verify(rateRepo, never()).save(any());
    }

    @Test void rollbackRate_whenHasPreviousVersion_restoresPreviousAndPopsHistory() {
        var previous = CommissionRate.CommissionRateVersion.builder()
            .codMarca(10)
            .descrMarca("PRETO")
            .codCargo(100)
            .descriCargo("VENDEDOR LOJA")
            .pctComiss(0.025)
            .data(LocalDate.of(2026, 3, 1))
            .versao(4)
            .textoOriginal("texto v4")
            .explicacao("exp v4")
            .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
            .updatedAt(LocalDateTime.of(2026, 4, 2, 10, 0))
            .isVigente(true)
            .deletedAt(null)
            .build();

        var current = CommissionRate.builder()
            .id("123")
            .codMarca(20)
            .descrMarca("CINZA")
            .codCargo(150)
            .descriCargo("GERENTE")
            .pctComiss(0.04)
            .data(LocalDate.of(2026, 4, 1))
            .versao(5)
            .textoOriginal("texto v5")
            .explicacao("exp v5")
            .createdAt(LocalDateTime.of(2026, 4, 3, 10, 0))
            .updatedAt(LocalDateTime.of(2026, 4, 4, 10, 0))
            .isVigente(false)
            .deletedAt(null)
            .versoesAnteriores(new java.util.ArrayList<>(List.of(previous)))
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(current));

        service.rollbackRate("123");

        assertThat(current.getVersao()).isEqualTo(4);
        assertThat(current.getCodMarca()).isEqualTo(10);
        assertThat(current.getDescrMarca()).isEqualTo("PRETO");
        assertThat(current.getCodCargo()).isEqualTo(100);
        assertThat(current.getDescriCargo()).isEqualTo("VENDEDOR LOJA");
        assertThat(current.getPctComiss()).isEqualTo(0.025);
        assertThat(current.getData()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(current.getTextoOriginal()).isEqualTo("texto v4");
        assertThat(current.getExplicacao()).isEqualTo("exp v4");
        assertThat(current.getIsVigente()).isTrue();
        assertThat(current.getVersoesAnteriores()).isEmpty();
        verify(rateRepo).save(current);
    }

    @Test void rollbackRate_whenNoPreviousVersion_isNoOp() {
        var current = CommissionRate.builder()
            .id("123")
            .versao(1)
            .versoesAnteriores(new java.util.ArrayList<>())
            .build();
        when(rateRepo.findById("123")).thenReturn(Optional.of(current));

        service.rollbackRate("123");

        verify(rateRepo, never()).save(any());
    }

    private CommissionRate validRate() {
        return CommissionRate.builder()
            .codMarca(10)
            .descrMarca("PRETO")
            .codCargo(100)
            .descriCargo("VENDEDOR LOJA")
            .pctComiss(0.025)
            .build();
    }
}
