package team.phoenix.backend.controller;

public record CreateBrandRequest(
    Integer codigo,
    String nome,
    String descricao
) {
}
