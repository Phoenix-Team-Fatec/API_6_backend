package team.phoenix.backend.service;

public record AiIngestRulesRequest(
    String pdf,
    boolean overwrite
) {}
