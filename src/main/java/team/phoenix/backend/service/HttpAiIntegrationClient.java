package team.phoenix.backend.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class HttpAiIntegrationClient implements AiIntegrationClient {

    private final RestClient restClient;

    HttpAiIntegrationClient(@Value("${commission.ai.base-url:http://127.0.0.1:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @Override
    public AiAgentResponse askAgent(String userInput) {
        return restClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/agent")
                .queryParam("user_input", userInput)
                .build())
            .retrieve()
            .body(AiAgentResponse.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> health() {
        return restClient.get()
            .uri("/health")
            .retrieve()
            .body(Map.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> ingestCode(AiIngestCodeRequest request) {
        return restClient.post()
            .uri("/ingestion/code")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(Map.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> ingestRules(AiIngestRulesRequest request) {
        return restClient.post()
            .uri("/ingestion/rules")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(Map.class);
    }
}
