package team.phoenix.backend.catalog.position.application;

import java.util.List;
import java.util.Optional;

import team.phoenix.backend.domain.model.Position;

public interface PositionService {

    List<Position> listPositions(Integer codigo, String nome, String descricao);

    Optional<Position> getPositionById(String id);

    Position createPosition(Position position);

    Position updatePosition(String id, Position updatedPosition);

    void deletePosition(String id);
}
