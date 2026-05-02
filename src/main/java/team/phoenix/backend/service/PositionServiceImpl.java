package team.phoenix.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.domain.model.Position;
import team.phoenix.backend.domain.repository.PositionRepository;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    @Override
    public List<Position> listPositions(Integer codigo, String nome, String descricao) {
        if (codigo != null) {
            return positionRepository.findByCodigo(codigo)
                .filter(p -> matchesNome(p, nome))
                .filter(p -> matchesDescricao(p, descricao))
                .map(List::of)
                .orElse(List.of());
        }
        if (nome != null && !nome.isBlank() && descricao != null && !descricao.isBlank()) {
            return positionRepository.findAll().stream()
                .filter(p -> matchesNome(p, nome))
                .filter(p -> matchesDescricao(p, descricao))
                .toList();
        }
        if (nome != null && !nome.isBlank()) {
            return positionRepository.findByNomeContainingIgnoreCase(nome);
        }
        if (descricao != null && !descricao.isBlank()) {
            return positionRepository.findByDescricaoContainingIgnoreCase(descricao);
        }
        return positionRepository.findAll();
    }

    @Override
    public Optional<Position> getPositionById(String id) {
        return positionRepository.findById(id);
    }

    @Override
    public Position createPosition(Position position) {
        validateRequiredFields(position.getCodigo(), position.getNome(), position.getDescricao());

        if (positionRepository.findByCodigo(position.getCodigo()).isPresent()) {
            throw new IllegalStateException("Cargo com código já existente: " + position.getCodigo());
        }

        position.setNome(position.getNome().trim());
        position.setDescricao(position.getDescricao().trim());
        position.setCreatedAt(LocalDateTime.now());
        position.setUpdatedAt(null);

        return positionRepository.save(position);
    }

    @Override
    public Position updatePosition(String id, Position updatedPosition) {
        Optional<Position> existing = positionRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Cargo não encontrado: " + id);
        }

        Position current = existing.get();

        if (updatedPosition.getCodigo() != null) {
            Optional<Position> byCodigo = positionRepository.findByCodigo(updatedPosition.getCodigo());
            if (byCodigo.isPresent() && !byCodigo.get().getId().equals(id)) {
                throw new IllegalStateException("Cargo com código já existente: " + updatedPosition.getCodigo());
            }
            current.setCodigo(updatedPosition.getCodigo());
        }

        if (updatedPosition.getDescricao() != null) {
            if (updatedPosition.getDescricao().isBlank()) {
                throw new IllegalArgumentException("Descrição do cargo é obrigatória");
            }
            current.setDescricao(updatedPosition.getDescricao().trim());
        }

        if (updatedPosition.getNome() != null) {
            if (updatedPosition.getNome().isBlank()) {
                throw new IllegalArgumentException("Nome do cargo é obrigatório");
            }
            current.setNome(updatedPosition.getNome().trim());
        }

        current.setUpdatedAt(LocalDateTime.now());

        return positionRepository.save(current);
    }

    @Override
    public void deletePosition(String id) {
        if (positionRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Cargo não encontrado: " + id);
        }
        positionRepository.deleteById(id);
    }

    private void validateRequiredFields(Integer codigo, String nome, String descricao) {
        if (codigo == null) {
            throw new IllegalArgumentException("Código do cargo é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do cargo é obrigatório");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição do cargo é obrigatória");
        }
    }

    private boolean matchesNome(Position position, String nome) {
        if (nome == null || nome.isBlank()) {
            return true;
        }
        return position.getNome() != null && position.getNome().toLowerCase().contains(nome.toLowerCase());
    }

    private boolean matchesDescricao(Position position, String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return true;
        }
        return position.getDescricao() != null && position.getDescricao().toLowerCase().contains(descricao.toLowerCase());
    }
}
