package team.phoenix.backend.catalog.brand.api;

import java.time.LocalDateTime;

import team.phoenix.backend.domain.model.Brand;

public record BrandResponse(
    String id,
    Integer codigo,
    String nome,
    String descricao,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(
            brand.getId(),
            brand.getCodigo(),
            brand.getNome(),
            brand.getDescricao(),
            brand.getCreatedAt(),
            brand.getUpdatedAt()
        );
    }
}
