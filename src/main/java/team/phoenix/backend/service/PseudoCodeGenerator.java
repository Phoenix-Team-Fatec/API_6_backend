package team.phoenix.backend.service;

import team.phoenix.backend.domain.model.CommissionRate;

// Requisito #11: Gerador de representação em código (pseudocódigo da regra)
public class PseudoCodeGenerator {

    public static String generate(CommissionRate rule) {
        if (rule == null) {
            return "";
        }

        String cargo = rule.getDescriCargo() != null ? rule.getDescriCargo() : "CARGO_INDEFINIDO";
        String marca = rule.getDescrMarca() != null ? rule.getDescrMarca() : "MARCA_INDEFINIDA";
        Double pct = rule.getPctComiss();
        double comissao = pct != null ? pct : 0.0;

        return String.format(
            "SE (cargo = '%s' AND marca = '%s')\n" +
            "  ENTÃO aplica comissão de %.2f%% sobre base de vendas\n" +
            "  TIPO: comissão simples por cargo e marca\n" +
            "  CONDIÇÃO: regra geral (sem exceções de férias, afastamento ou demissão)\n" +
            "FIM",
            cargo, marca, comissao * 100
        );
    }
}
