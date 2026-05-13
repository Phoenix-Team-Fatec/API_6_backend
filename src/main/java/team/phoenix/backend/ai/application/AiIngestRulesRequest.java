package team.phoenix.backend.ai.application;

public record AiIngestRulesRequest(
    String pdf,
    boolean overwrite
) {}
