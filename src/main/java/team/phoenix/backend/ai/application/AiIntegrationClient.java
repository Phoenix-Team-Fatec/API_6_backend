package team.phoenix.backend.ai.application;

import java.util.Map;

public interface AiIntegrationClient {
    AiAgentResponse askAgent(String userInput);
    Map<String, Object> health();
    Map<String, Object> ingestCode(AiIngestCodeRequest request);
    Map<String, Object> ingestRules(AiIngestRulesRequest request);
}
