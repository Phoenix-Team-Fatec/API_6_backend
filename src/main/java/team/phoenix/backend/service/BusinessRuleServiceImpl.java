package team.phoenix.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import team.phoenix.backend.domain.model.BusinessRule;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.MonthlyException;
import team.phoenix.backend.domain.repository.BusinessRuleRepository;
import team.phoenix.backend.domain.repository.MonthlyExceptionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// Serviço que envia texto em linguagem natural para ML e persiste a regra interpretada
@Service
@RequiredArgsConstructor
public class BusinessRuleServiceImpl implements BusinessRuleService {

    private final BusinessRuleRepository repository;
    private final MonthlyExceptionRepository exceptionRepository;
    private final RestTemplate restTemplate;

    @Value("${ml.api.url}")
    private String mlApiUrl;

    @Override
    public BusinessRule create(String rawText) {
        return create(rawText, null);
    }

    @Override
    public BusinessRule create(String rawText, String yearMonth) {
        Map<?, ?> interpretation;
        try {
            String requestText = yearMonth != null
                ? rawText + " (mês de competência: " + yearMonth + ")"
                : rawText;
            var body = Map.of("request", requestText);
            @SuppressWarnings("unchecked")
            var result = restTemplate.postForObject(mlApiUrl + "/interpret-rule", body, Map.class);
            interpretation = result;
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "ML service is unavailable: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Error calling ML service: " + e.getMessage());
        }

        if (interpretation == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ML service returned empty response");
        }

        createMonthlyException(interpretation);

        int version = (int) repository.count() + 1;

        BusinessRule rule = BusinessRule.builder()
            .rawText(rawText)
            .changeSummary((String) interpretation.get("summary"))
            .targetFile(null)
            .diff(null)
            .updatedCode(null)
            .version(version)
            .createdAt(LocalDateTime.now())
            .build();

        return repository.save(rule);
    }

    @Override
    public List<BusinessRule> listAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    private void createMonthlyException(Map<?, ?> interpretation) {
        String typeStr = (String) interpretation.get("type");
        String yearMonth = (String) interpretation.get("year_month");

        if (typeStr == null || yearMonth == null) return;

        ExceptionType type;
        try {
            type = ExceptionType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return; // unknown type — skip
        }

        MonthlyException exception = MonthlyException.builder()
            .yearMonth(yearMonth)
            .type(type)
            .matricula((String) interpretation.get("matricula"))
            .codMarca(toInteger(interpretation.get("cod_marca")))
            .codCargo(toInteger(interpretation.get("cod_cargo")))
            .overrideRate(toDouble(interpretation.get("override_rate")))
            .amount(toDouble(interpretation.get("amount")))
            .startDate(toDate(interpretation.get("start_date")))
            .endDate(toDate(interpretation.get("end_date")))
            .appliesToManagers(Boolean.TRUE.equals(interpretation.get("applies_to_managers")))
            .build();

        exceptionRepository.save(exception);
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number n) return n.intValue();
        return null;
    }

    private Double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        return null;
    }

    private LocalDate toDate(Object value) {
        if (value instanceof String s && !s.isBlank()) return LocalDate.parse(s);
        return null;
    }
}
