package team.phoenix.backend.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiVenda(
    String matricula,
    @JsonProperty("cod_marca") Integer codMarca,
    @JsonProperty("cod_loja") String codLoja,
    @JsonProperty("vlr_venda") double vlrVenda
) {}
