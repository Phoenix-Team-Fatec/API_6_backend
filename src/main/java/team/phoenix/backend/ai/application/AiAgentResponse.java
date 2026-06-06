package team.phoenix.backend.ai.application;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiAgentResponse(
    String tipo,
    AiAgentOverride override,
    List<AiAgentIntercorrencia> intercorrencias,
    @JsonProperty("rate_overrides") List<AiScopedRateRule> rateOverrides,
    String justificativa,
    @JsonProperty("token_usage") Map<String, Integer> tokenUsage
) {
    public AiAgentResponse(
            String tipo,
            AiAgentOverride override,
            List<AiAgentIntercorrencia> intercorrencias,
            String justificativa) {
        this(tipo, override, intercorrencias, null, justificativa, null);
    }
}
