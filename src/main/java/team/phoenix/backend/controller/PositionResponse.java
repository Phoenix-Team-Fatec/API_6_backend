package team.phoenix.backend.controller;

import java.time.LocalDateTime;

import team.phoenix.backend.domain.model.Position;

public record PositionResponse(
    String id,
    Integer codigo,
    String nome,
    String descricao,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PositionResponse from(Position position) {
        return new PositionResponse(
            position.getId(),
            position.getCodigo(),
            position.getNome(),
            position.getDescricao(),
            position.getCreatedAt(),
            position.getUpdatedAt()
        );
    }
}
