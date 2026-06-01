package team.phoenix.backend.ai.application;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiScopedRateRule(
    String descricao,
    @JsonProperty("vigencia_inicio") LocalDate vigenciaInicio,
    @JsonProperty("vigencia_fim") LocalDate vigenciaFim,
    AiRuleScope escopo,
    AiRuleEffect efeito
) {}
