package team.phoenix.backend.commission.application;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiFuncionario(
    String matricula,
    @JsonProperty("cod_marca") Integer codMarca,
    @JsonProperty("descr_marca") String descrMarca,
    @JsonProperty("cod_loja") String codLoja,
    @JsonProperty("descr_loja") String descrLoja,
    @JsonProperty("data_admissao") LocalDate dataAdmissao,
    @JsonProperty("data_demissao") LocalDate dataDemissao,
    @JsonProperty("cod_cargo") Integer codCargo,
    @JsonProperty("descr_cargo") String descrCargo
) {}
