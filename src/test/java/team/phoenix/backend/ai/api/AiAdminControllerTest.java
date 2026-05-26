package team.phoenix.backend.ai.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import team.phoenix.backend.ai.application.AiIngestCodeRequest;
import team.phoenix.backend.ai.application.AiIngestRulesRequest;
import team.phoenix.backend.ai.application.AiIntegrationClient;

@WebMvcTest(AiAdminController.class)
class AiAdminControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AiIntegrationClient aiIntegrationClient;

    @Test void health_returnsAiHealth() throws Exception {
        when(aiIntegrationClient.health()).thenReturn(Map.of("status", "ok"));

        mockMvc.perform(get("/api/ai/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test void ingestCode_proxiesRequestToAi() throws Exception {
        when(aiIntegrationClient.ingestCode(new AiIngestCodeRequest("C:/repo/src", true)))
            .thenReturn(Map.of("message", "codigo ingerido"));

        mockMvc.perform(post("/api/ai/ingestion/code")
                .contentType("application/json")
                .content("{\"root\":\"C:/repo/src\",\"overwrite\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("codigo ingerido"));

        verify(aiIntegrationClient).ingestCode(new AiIngestCodeRequest("C:/repo/src", true));
    }

    @Test void ingestRules_proxiesRequestToAi() throws Exception {
        when(aiIntegrationClient.ingestRules(new AiIngestRulesRequest("C:/rules.pdf", true)))
            .thenReturn(Map.of("message", "regras ingeridas"));

        mockMvc.perform(post("/api/ai/ingestion/rules")
                .contentType("application/json")
                .content("{\"pdf\":\"C:/rules.pdf\",\"overwrite\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("regras ingeridas"));

        verify(aiIntegrationClient).ingestRules(new AiIngestRulesRequest("C:/rules.pdf", true));
    }
}
