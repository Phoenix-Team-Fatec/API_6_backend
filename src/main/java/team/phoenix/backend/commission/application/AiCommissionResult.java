package team.phoenix.backend.commission.application;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiCommissionResult(
    String matricula,
    @JsonProperty("cod_loja") String codLoja,
    @JsonProperty("cod_marca") Integer codMarca,
    @JsonProperty("base_vendas") double baseVendas,
    @JsonProperty("perc_comissao") double percComissao,
    @JsonProperty("valor_comissao_bruto") double valorComissaoBruto,
    @JsonProperty("ajuste_proporcional") double ajusteProporcional,
    double bonus,
    @JsonProperty("valor_final") double valorFinal
) {}
