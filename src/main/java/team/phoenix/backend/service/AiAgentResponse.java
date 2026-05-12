package team.phoenix.backend.service;

import java.util.List;

public record AiAgentResponse(
    String tipo,
    AiAgentOverride override,
    List<AiAgentIntercorrencia> intercorrencias,
    String justificativa
) {}
