package team.phoenix.backend.controller;

import java.time.LocalDateTime;

import team.phoenix.backend.domain.model.Store;

public record StoreResponse(
    String id,
    Integer codigo,
    String nome,
    String descricao,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static StoreResponse from(Store store) {
        return new StoreResponse(
            store.getId(),
            store.getCodigo(),
            store.getNome(),
            store.getDescricao(),
            store.getCreatedAt(),
            store.getUpdatedAt()
        );
    }
}
