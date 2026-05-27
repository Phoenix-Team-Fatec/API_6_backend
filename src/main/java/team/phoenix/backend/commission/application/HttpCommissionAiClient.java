package team.phoenix.backend.commission.application;

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
import com.fasterxml.jackson.annotation.JsonProperty;

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
    public List<AiCommissionResult> calculate(AiCommissionRequest request, int year, int month, boolean auditoria) {
        Map<String, Object> body = Map.of(
            "regras_mongo", request.regrasMongo(),
            "funcionarios", request.funcionarios(),
            "vendas", request.vendas(),
            "tabela_comissao", request.tabelaComissao()
        );
        String jsonBody = toJson(body);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/commission-algorithm?ano=" + year + "&mes=" + month + "&auditoria=" + auditoria))
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

        String response = httpResponse.body();
        System.out.println("=== API Python Response ===");
        System.out.println(response);
        System.out.println("============================");

        // Então tente fazer parse
        AiCommissionResult[] results = fromJson(response);

        return results == null ? List.of() : Arrays.asList(results);
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
            // Tenta fazer parse como resposta wrapper do Python
            AiCommissionApiResponse response = objectMapper.readValue(body, AiCommissionApiResponse.class);
            
            if (response.resultados() == null || response.resultados().isEmpty()) {
                return new AiCommissionResult[0];
            }
            
            // Converte de AiCommissionResultWrapper para AiCommissionResult
            return response.resultados().stream()
                .map(wrapper -> new AiCommissionResult(
                    wrapper.matricula(),
                    wrapper.resultadoFinal().codLoja(),
                    wrapper.resultadoFinal().codMarca(),
                    wrapper.resultadoFinal().baseVendas(),
                    wrapper.resultadoFinal().percComissao(),
                    wrapper.resultadoFinal().valorComissaoBruto(),
                    wrapper.resultadoFinal().ajusteProporcional(),
                    wrapper.resultadoFinal().bonus(),
                    wrapper.resultadoFinal().valorFinal(),
                    wrapper.auditoria() != null ? wrapper.auditoria().etapas() : null
                ))
                .toArray(AiCommissionResult[]::new);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse AI commission response", e);
        }
    }
}

record AiCommissionApiResponse(
    boolean sucesso,
    @JsonProperty("total_funcionarios") int totalFuncionarios,
    int ano,
    int mes,
    List<AiCommissionResultWrapper> resultados
) {
    public int total_funcionarios() { return totalFuncionarios; }
}

record AiCommissionResultWrapper(
    String matricula,
    @JsonProperty("resultado_final") AiCommissionResultadoFinal resultadoFinal,
    AiCommissionAuditoria auditoria
) {
    public String matricula() { return matricula; }
    public AiCommissionResultadoFinal resultado_final() { return resultadoFinal; }
    public AiCommissionAuditoria auditoria() { return auditoria; }
}

record AiCommissionResultadoFinal(
    @JsonProperty("base_vendas") double baseVendas,
    @JsonProperty("perc_comissao") double percComissao,
    @JsonProperty("valor_comissao_bruto") double valorComissaoBruto,
    @JsonProperty("ajuste_proporcional") double ajusteProporcional,
    double bonus,
    @JsonProperty("valor_final") double valorFinal,
    @JsonProperty("cod_loja") String codLoja,
    @JsonProperty("cod_marca") Integer codMarca
) {
    public double base_vendas() { return baseVendas; }
    public double perc_comissao() { return percComissao; }
    public double valor_comissao_bruto() { return valorComissaoBruto; }
    public double ajuste_proporcional() { return ajusteProporcional; }
    public double bonus() { return bonus; }
    public double valor_final() { return valorFinal; }
    public String cod_loja() { return codLoja; }
    public Integer cod_marca() { return codMarca; }
}

record AiCommissionAuditoria(
    @JsonProperty("total_etapas") int totalEtapas,
    List<EtapaCalculo> etapas
) {
    public int total_etapas() { return totalEtapas; }
}
