package team.phoenix.backend.rules.application;

import java.time.LocalDate;

import team.phoenix.backend.domain.model.CommissionRate;

// Requisito #10: Gerador de representação textual da regra
public class TextualRuleGenerator {

    public static String generate(CommissionRate rule) {
        if (rule == null) {
            return "";
        }

        String cargo = rule.getDescriCargo() != null ? rule.getDescriCargo() : "funcionário";
        String marca = rule.getDescrMarca() != null ? rule.getDescrMarca() : "marca";
        double pct = rule.getPctComiss() != null ? rule.getPctComiss() : 0.0;
        double comissao = pct * 100;
        
        String dataStr = "data indefinida";
        if (rule.getData() != null) {
            LocalDate date = rule.getData();
            dataStr = formatarMes(date.getMonthValue()) + " de " + date.getYear();
        }

        return String.format(
            "%s da marca %s recebem %.2f%% de comissão a partir de %s",
            cargo, marca, comissao, dataStr
        ).replaceAll(",00%", "%");
    }

    private static String formatarMes(int mes) {
        String[] meses = {
            "", "janeiro", "fevereiro", "março", "abril", "maio", "junho",
            "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
        };
        return mes >= 1 && mes <= 12 ? meses[mes] : "mês indefinido";
    }
}
