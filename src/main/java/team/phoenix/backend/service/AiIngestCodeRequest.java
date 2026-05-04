package team.phoenix.backend.service;

public record AiIngestCodeRequest(
    String root,
    boolean overwrite
) {}
