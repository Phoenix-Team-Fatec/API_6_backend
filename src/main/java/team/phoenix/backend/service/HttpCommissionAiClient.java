package team.phoenix.backend.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class HttpCommissionAiClient implements CommissionAiClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    HttpCommissionAiClient(
            @Value("${commission.ai.base-url:http://127.0.0.1:8000}") String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public List<AiCommissionResult> calculate(AiCommissionRequest request, int year, int month) {
        Map<String, Object> body = Map.of(
            "regras_mongo", request.regrasMongo(),
            "funcionarios", request.funcionarios(),
            "vendas", request.vendas(),
            "tabela_comissao", request.tabelaComissao()
        );
        String jsonBody = toJson(body);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/commission-algorithm?ano=" + year + "&mes=" + month))
            .version(HttpClient.Version.HTTP_1_1)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> httpResponse;
        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call AI commission algorithm", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling AI commission algorithm", e);
        }

        if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
            throw new IllegalStateException(
                "AI commission algorithm returned " + httpResponse.statusCode() + ": " + httpResponse.body());
        }

        AiCommissionResult[] response = fromJson(httpResponse.body());

        return response == null ? List.of() : Arrays.asList(response);
    }

    private String toJson(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize AI commission request", e);
        }
    }

    private AiCommissionResult[] fromJson(String body) {
        try {
            return objectMapper.readValue(body, AiCommissionResult[].class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse AI commission response", e);
        }
    }
}
