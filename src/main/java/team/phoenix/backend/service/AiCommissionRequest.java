package team.phoenix.backend.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import team.phoenix.backend.domain.model.CommissionRate;
import team.phoenix.backend.domain.model.ExceptionType;
import team.phoenix.backend.domain.model.HrRecord;
import team.phoenix.backend.domain.model.MonthlyException;
import team.phoenix.backend.domain.model.RateType;
import team.phoenix.backend.domain.model.SalesRecord;

public record AiCommissionRequest(
    @JsonProperty("regras_mongo") List<Map<String, Object>> regrasMongo,
    List<AiFuncionario> funcionarios,
    List<AiVenda> vendas,
    @JsonProperty("tabela_comissao") List<AiComissionamentoBase> tabelaComissao
) {
    public static AiCommissionRequest from(
            List<HrRecord> employees,
            List<SalesRecord> sales,
            List<CommissionRate> rates,
            List<MonthlyException> exceptions) {
        return from(employees, sales, rates, exceptions, List.of(), null);
    }

    public static AiCommissionRequest from(
            List<HrRecord> employees,
            List<SalesRecord> sales,
            List<CommissionRate> rates,
            List<MonthlyException> exceptions,
            List<CommissionRate> monthlyOverrides,
            LocalDate month) {
        return new AiCommissionRequest(
            mapRules(exceptions, monthlyOverrides, month),
            employees.stream().map(AiCommissionRequest::mapEmployee).toList(),
            sales.stream().map(AiCommissionRequest::mapSale).toList(),
            rates.stream().map(AiCommissionRequest::mapRate).toList()
        );
    }

    private static AiFuncionario mapEmployee(HrRecord hr) {
        return new AiFuncionario(
            hr.getMatricula(),
            hr.getCodMarca(),
            hr.getDescrMarca(),
            String.valueOf(hr.getCodLoja()),
            hr.getDescrLoja(),
            hr.getDataAdmiss(),
            hr.getDataDemiss(),
            hr.getCodCargo(),
            hr.getDescriCargo()
        );
    }

    private static AiVenda mapSale(SalesRecord sale) {
        return new AiVenda(
            sale.getMatricula(),
            sale.getCodMarca(),
            String.valueOf(sale.getCodLoja()),
            sale.getVlrVenda() == null ? 0.0 : sale.getVlrVenda()
        );
    }

    private static AiComissionamentoBase mapRate(CommissionRate rate) {
        return new AiComissionamentoBase(
            rate.getCodMarca(),
            rate.getCodCargo(),
            rate.getPctComiss() == null ? 0.0 : rate.getPctComiss()
        );
    }

    private static List<Map<String, Object>> mapRules(
            List<MonthlyException> exceptions,
            List<CommissionRate> monthlyOverrides,
            LocalDate month) {
        var rules = new java.util.ArrayList<Map<String, Object>>();
        Map<String, Object> override = mapOverrides(monthlyOverrides, month);
        if (override != null) {
            rules.add(override);
        }

        Map<String, Object> exception = mapExceptions(exceptions);
        if (exception != null) {
            rules.add(exception);
        }

        return List.copyOf(rules);
    }

    private static Map<String, Object> mapOverrides(List<CommissionRate> monthlyOverrides, LocalDate month) {
        if (month == null || monthlyOverrides == null || monthlyOverrides.isEmpty()) {
            return null;
        }

        Map<String, Object> percOverride = new LinkedHashMap<>();
        for (CommissionRate rate : monthlyOverrides) {
            if (rate.getCodMarca() == null || rate.getCodCargo() == null || rate.getPctComiss() == null) {
                continue;
            }
            percOverride.put(rate.getCodMarca() + "," + rate.getCodCargo(), rate.getPctComiss());
        }

        if (percOverride.isEmpty()) {
            return null;
        }

        Map<String, Object> override = new LinkedHashMap<>();
        override.put("descricao", "Regras de comissao vigentes para " + month);
        override.put("data_inicio", month.withDayOfMonth(1));
        override.put("data_fim", month.withDayOfMonth(month.lengthOfMonth()));
        override.put("perc_override", percOverride);
        override.put("marca_override", Map.of());
        override.put("perc_adicional", Map.of());

        Map<String, Object> document = new LinkedHashMap<>();
        document.put("tipo", "override");
        document.put("override", override);
        return document;
    }

    private static Map<String, Object> mapExceptions(List<MonthlyException> exceptions) {
        var items = exceptions.stream()
            .map(AiCommissionRequest::mapException)
            .filter(Objects::nonNull)
            .toList();

        if (items.isEmpty()) {
            return null;
        }

        Map<String, Object> document = new LinkedHashMap<>();
        document.put("tipo", "intercorrencia");
        document.put("intercorrencias", items);
        return document;
    }

    private static Map<String, Object> mapException(MonthlyException exception) {
        String type = mapExceptionType(exception);
        if (type == null || exception.getMatricula() == null || exception.getMatricula().isBlank()) {
            return null;
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("matricula", exception.getMatricula());
        item.put("tipo", type);
        item.put("valor", resolveExceptionValue(exception));
        item.put("vigencia_inicio", resolveStartDate(exception));
        item.put("vigencia_fim", resolveEndDate(exception));
        return item;
    }

    private static String mapExceptionType(MonthlyException exception) {
        if (exception.getType() == null) {
            return null;
        }
        return switch (exception.getType()) {
            case BONUS_FIXED -> "bonus_fixo";
            case RATE_OVERRIDE -> exception.getRateType() == RateType.ADDITIVE ? "perc_bonus" : null;
            case SALES_BONUS_TIER -> exception.getAmount() == null ? null : "bonus_venda";
            case ABSENCE, VACATION, MATERNITY_LEAVE, STORE_BONUS_TIER, MULTI_STORE -> null;
        };
    }

    private static double resolveExceptionValue(MonthlyException exception) {
        if (exception.getType() == ExceptionType.RATE_OVERRIDE) {
            return exception.getOverrideRate() == null ? 0.0 : exception.getOverrideRate();
        }
        return exception.getAmount() == null ? 0.0 : exception.getAmount();
    }

    private static LocalDate resolveStartDate(MonthlyException exception) {
        return exception.getStartDate() == null ? exception.getYearMonth() : exception.getStartDate();
    }

    private static LocalDate resolveEndDate(MonthlyException exception) {
        return exception.getEndDate() == null ? exception.getYearMonth() : exception.getEndDate();
    }
}
