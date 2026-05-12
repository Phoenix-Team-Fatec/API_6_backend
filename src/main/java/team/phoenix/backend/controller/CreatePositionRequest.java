package team.phoenix.backend.controller;

public record CreatePositionRequest(
    Integer codigo,
    String nome,
    String descricao
) {
}
