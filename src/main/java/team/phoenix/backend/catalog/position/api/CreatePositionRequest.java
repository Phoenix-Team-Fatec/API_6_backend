package team.phoenix.backend.catalog.position.api;

public record CreatePositionRequest(
    Integer codigo,
    String nome,
    String descricao
) {
}
