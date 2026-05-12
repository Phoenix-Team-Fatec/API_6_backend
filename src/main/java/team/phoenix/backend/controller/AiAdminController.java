package team.phoenix.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.service.AiIngestCodeRequest;
import team.phoenix.backend.service.AiIngestRulesRequest;
import team.phoenix.backend.service.AiIntegrationClient;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
@RequiredArgsConstructor
public class AiAdminController {

    private final AiIntegrationClient aiIntegrationClient;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(aiIntegrationClient.health());
    }

    @PostMapping("/ingestion/code")
    public ResponseEntity<Map<String, Object>> ingestCode(@RequestBody AiIngestCodeRequest request) {
        return ResponseEntity.ok(aiIntegrationClient.ingestCode(request));
    }

    @PostMapping("/ingestion/rules")
    public ResponseEntity<Map<String, Object>> ingestRules(@RequestBody AiIngestRulesRequest request) {
        return ResponseEntity.ok(aiIntegrationClient.ingestRules(request));
    }
}
