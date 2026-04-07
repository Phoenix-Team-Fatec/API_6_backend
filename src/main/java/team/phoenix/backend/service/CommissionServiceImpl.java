package team.phoenix.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import team.phoenix.backend.domain.model.*;
import team.phoenix.backend.domain.repository.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

// Orquestra o cálculo de comissão delegando ao serviço de ML
@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final CommissionRateRepository rateRepo;
    private final HrRecordRepository hrRepo;
    private final SalesRecordRepository salesRepo;
    private final MonthlyExceptionRepository exceptionRepo;
    private final RestTemplate restTemplate;

    @Value("${ml.api.url}")
    private String mlApiUrl;

    @Override
    public CommissionResult simulate(String matricula, YearMonth month) {
        LocalDate ref = month.atDay(1);

        HrRecord hr = hrRepo.findByMatriculaAndDataRef(matricula, ref)
            .orElseThrow(() -> new RuntimeException(
                "HR record not found: matricula=" + matricula + " month=" + month));

        List<SalesRecord> allSales = salesRepo.findByDateRef(ref);
        List<CommissionRate> rates = rateRepo.findAll();
        List<MonthlyException> exceptions = exceptionRepo.findByYearMonth(month.toString());

        Map<String, Object> payload = buildPayload(hr, allSales, rates, exceptions, month);

        List<?> mlResults;
        try {
            mlResults = restTemplate.postForObject(mlApiUrl + "/simulate", payload, List.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "ML service unavailable: " + e.getMessage());
        }

        if (mlResults == null || mlResults.isEmpty()) {
            throw new RuntimeException("ML returned empty response for matricula=" + matricula);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = mlResults.stream()
            .map(r -> (Map<String, Object>) r)
            .filter(r -> matricula.equals(r.get("matricula")))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "ML returned no result for matricula=" + matricula));

        return toCommissionResult(result, hr, month);
    }

    // -------------------------------------------------------------------------
    // Payload builder
    // -------------------------------------------------------------------------

    private Map<String, Object> buildPayload(
        HrRecord hr,
        List<SalesRecord> allSales,
        List<CommissionRate> rates,
        List<MonthlyException> exceptions,
        YearMonth month
    ) {
        List<Map<String, Object>> tabelaComissao = buildTabelaComissao(rates, exceptions);
        List<Map<String, Object>> intercorrencias = buildIntercorrencias(exceptions);

        Map<String, Object> funcionario = new HashMap<>();
        funcionario.put("matricula", hr.getMatricula());
        funcionario.put("cod_marca", hr.getCodMarca());
        funcionario.put("descr_marca", hr.getDescrMarca());
        funcionario.put("cod_loja", String.valueOf(hr.getCodLoja()));
        funcionario.put("descr_loja", hr.getDescrLoja());
        funcionario.put("data_admissao", hr.getDataAdmiss().toString());
        funcionario.put("data_demissao", hr.getDataDemiss() != null ? hr.getDataDemiss().toString() : null);
        funcionario.put("cod_cargo", hr.getCodCargo());
        funcionario.put("descr_cargo", hr.getDescriCargo());

        List<Map<String, Object>> vendas = allSales.stream()
            .map(v -> Map.<String, Object>of(
                "matricula", v.getMatricula(),
                "cod_marca", v.getCodMarca(),
                "cod_loja", String.valueOf(v.getCodLoja()),
                "vlr_venda", v.getVlrVenda()
            )).toList();

        Map<String, Object> payload = new HashMap<>();
        payload.put("funcionarios", List.of(funcionario));
        payload.put("vendas", vendas);
        payload.put("tabela_comissao", tabelaComissao);
        payload.put("intercorrencias", intercorrencias);
        payload.put("ano", month.getYear());
        payload.put("mes", month.getMonthValue());
        return payload;
    }

    // Builds commission table applying RATE_OVERRIDE exceptions directly
    private List<Map<String, Object>> buildTabelaComissao(
        List<CommissionRate> rates,
        List<MonthlyException> exceptions
    ) {
        Map<String, Double> overrides = new HashMap<>();
        for (MonthlyException ex : exceptions) {
            if (ex.getType() == ExceptionType.RATE_OVERRIDE && ex.getCodMarca() != null && ex.getCodCargo() != null) {
                overrides.put(ex.getCodMarca() + ":" + ex.getCodCargo(), ex.getOverrideRate());
            }
        }

        return rates.stream().map(r -> {
            String key = r.getCodMarca() + ":" + r.getCodCargo();
            double perc = overrides.getOrDefault(key, r.getPctComiss());
            return Map.<String, Object>of(
                "cod_marca", r.getCodMarca(),
                "cod_cargo", r.getCodCargo(),
                "perc_comissao", perc
            );
        }).toList();
    }

    // Maps MonthlyException records to ML intercorrencia format
    private List<Map<String, Object>> buildIntercorrencias(List<MonthlyException> exceptions) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (MonthlyException ex : exceptions) {
            switch (ex.getType()) {
                case ABSENCE -> {
                    Map<String, Object> ic = new HashMap<>();
                    ic.put("matricula", ex.getMatricula());
                    ic.put("tipo", "afastamento");
                    ic.put("data_inicio", ex.getStartDate().toString());
                    ic.put("data_fim", ex.getEndDate().toString());
                    ic.put("valor", 0.0);
                    result.add(ic);
                }
                case VACATION -> {
                    Map<String, Object> ic = new HashMap<>();
                    ic.put("matricula", ex.getMatricula());
                    ic.put("tipo", "ferias");
                    ic.put("data_inicio", ex.getStartDate().toString());
                    ic.put("data_fim", ex.getEndDate().toString());
                    ic.put("valor", 0.0);
                    result.add(ic);
                }
                case BONUS_FIXED -> {
                    Map<String, Object> ic = new HashMap<>();
                    ic.put("matricula", ex.getMatricula());
                    ic.put("tipo", "bonus_fixo");
                    ic.put("valor", ex.getAmount() != null ? ex.getAmount() : 0.0);
                    result.add(ic);
                }
                default -> { /* RATE_OVERRIDE handled in tabela_comissao; others pending */ }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Response mapper
    // -------------------------------------------------------------------------

    private CommissionResult toCommissionResult(Map<String, Object> r, HrRecord hr, YearMonth month) {
        double baseVendas = toDouble(r.get("base_vendas"));
        double percComissao = toDouble(r.get("perc_comissao"));
        double valorBruto = toDouble(r.get("valor_comissao_bruto"));
        double fator = toDouble(r.get("ajuste_proporcional"));
        double bonus = toDouble(r.get("bonus"));
        double valorFinal = toDouble(r.get("valor_final"));

        boolean isGerente = hr.getCodCargo() == 150;
        String ruleApplied = isGerente ? "GERENTE" : "GERAL";
        if (fator < 1.0) ruleApplied += " (proporcional)";

        List<String> bonusList = bonus > 0
            ? List.of(String.format("Bônus: R$%.2f", bonus))
            : List.of();

        String explanation = String.format("%.2f x %.4f x %.2f (fator) = %.2f",
            baseVendas, percComissao, fator, valorBruto * fator);

        return new CommissionResult(
            hr.getMatricula(), month.toString(), hr,
            baseVendas, percComissao, valorBruto * fator,
            bonusList, bonus, valorFinal,
            ruleApplied, explanation
        );
    }

    private double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        return 0.0;
    }
}
