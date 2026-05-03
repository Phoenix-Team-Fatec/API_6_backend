package team.phoenix.backend.service;

import java.time.LocalDate;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiAgentOverride(
    String descricao,
    @JsonProperty("data_inicio") LocalDate dataInicio,
    @JsonProperty("data_fim") LocalDate dataFim,
    @JsonProperty("perc_override") Map<String, Double> percOverride,
    @JsonProperty("marca_override") Map<String, Integer> marcaOverride,
    @JsonProperty("perc_adicional") Map<String, Double> percAdicional
) {}
