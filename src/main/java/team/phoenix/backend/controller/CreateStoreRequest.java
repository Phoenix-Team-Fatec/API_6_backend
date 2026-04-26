package team.phoenix.backend.controller;

public record CreateStoreRequest(
    Integer codigo,
    String nome,
    String descricao
) {
}
