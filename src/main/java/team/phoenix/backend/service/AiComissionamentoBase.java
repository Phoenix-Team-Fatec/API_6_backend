package team.phoenix.backend.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiComissionamentoBase(
    @JsonProperty("cod_marca") Integer codMarca,
    @JsonProperty("cod_cargo") Integer codCargo,
    @JsonProperty("perc_comissao") double percComissao
) {}
