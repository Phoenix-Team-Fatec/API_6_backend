package team.phoenix.backend.ai.application;

import java.util.List;

public record AiAgentResponse(
    String tipo,
    AiAgentOverride override,
    List<AiAgentIntercorrencia> intercorrencias,
    String justificativa
) {}
