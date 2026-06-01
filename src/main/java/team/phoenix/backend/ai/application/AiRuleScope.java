package team.phoenix.backend.ai.application;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiRuleScope(
    String matricula,
    @JsonProperty("cod_loja") Integer codLoja,
    @JsonProperty("cod_marca") Integer codMarca,
    @JsonProperty("cod_cargo") Integer codCargo
) {}
