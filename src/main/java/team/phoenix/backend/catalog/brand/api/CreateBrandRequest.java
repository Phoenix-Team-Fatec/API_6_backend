package team.phoenix.backend.catalog.brand.api;

public record CreateBrandRequest(
    Integer codigo,
    String nome,
    String descricao
) {
}
