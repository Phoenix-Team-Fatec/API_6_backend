package team.phoenix.backend.catalog.position.application;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.audit.application.AuditLogService;
import team.phoenix.backend.audit.domain.AuditAction;
import team.phoenix.backend.audit.domain.AuditLogEntry;
import team.phoenix.backend.audit.domain.AuditResourceType;
import team.phoenix.backend.domain.model.Position;
import team.phoenix.backend.domain.repository.PositionRepository;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final AuditLogService auditLogService;

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

        Position saved = positionRepository.save(position);
        auditLogService.record(new AuditLogEntry(
            AuditAction.CREATE,
            AuditResourceType.POSITION,
            saved.getId(),
            "Cargo criado",
            positionMetadata(saved)
        ));
        return saved;
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

        Position saved = positionRepository.save(current);
        auditLogService.record(new AuditLogEntry(
            AuditAction.UPDATE,
            AuditResourceType.POSITION,
            saved.getId(),
            "Cargo atualizado",
            positionMetadata(saved)
        ));
        return saved;
    }

    @Override
    public void deletePosition(String id) {
        Optional<Position> existing = positionRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Cargo não encontrado: " + id);
        }
        positionRepository.deleteById(id);
        auditLogService.record(new AuditLogEntry(
            AuditAction.DELETE,
            AuditResourceType.POSITION,
            id,
            "Cargo removido",
            positionMetadata(existing.get())
        ));
    }

    private Map<String, Object> positionMetadata(Position position) {
        var metadata = new HashMap<String, Object>();
        putIfPresent(metadata, "codigo", position.getCodigo());
        putIfPresent(metadata, "nome", position.getNome());
        putIfPresent(metadata, "descricao", position.getDescricao());
        return metadata;
    }

    private void putIfPresent(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
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
