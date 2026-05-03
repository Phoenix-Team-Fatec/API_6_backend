package team.phoenix.backend.service;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiAgentIntercorrencia(
    String matricula,
    String tipo,
    double valor,
    @JsonProperty("vigencia_inicio") LocalDate vigenciaInicio,
    @JsonProperty("vigencia_fim") LocalDate vigenciaFim
) {}
