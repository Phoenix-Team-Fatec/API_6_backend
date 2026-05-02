package team.phoenix.backend.catalog.store.api;

public record CreateStoreRequest(
    Integer codigo,
    String nome,
    String descricao
) {
}
