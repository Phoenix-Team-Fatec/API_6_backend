package team.phoenix.backend.ai.application;

public record AiIngestCodeRequest(
    String root,
    boolean overwrite
) {}
