package team.phoenix.backend.service;

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
import team.phoenix.backend.domain.model.Store;
import team.phoenix.backend.domain.repository.StoreRepository;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final AuditLogService auditLogService;

    @Override
    public List<Store> listStores(Integer codigo, String nome, String descricao) {
        if (codigo != null) {
            return storeRepository.findByCodigo(codigo)
                .filter(s -> matchesNome(s, nome))
                .filter(s -> matchesDescricao(s, descricao))
                .map(List::of)
                .orElse(List.of());
        }
        if (nome != null && !nome.isBlank() && descricao != null && !descricao.isBlank()) {
            return storeRepository.findAll().stream()
                .filter(s -> matchesNome(s, nome))
                .filter(s -> matchesDescricao(s, descricao))
                .toList();
        }
        if (nome != null && !nome.isBlank()) {
            return storeRepository.findByNomeContainingIgnoreCase(nome);
        }
        if (descricao != null && !descricao.isBlank()) {
            return storeRepository.findByDescricaoContainingIgnoreCase(descricao);
        }
        return storeRepository.findAll();
    }

    @Override
    public Optional<Store> getStoreById(String id) {
        return storeRepository.findById(id);
    }

    @Override
    public Store createStore(Store store) {
        validateRequiredFields(store.getCodigo(), store.getNome(), store.getDescricao());

        if (storeRepository.findByCodigo(store.getCodigo()).isPresent()) {
            throw new IllegalStateException("Loja com código já existente: " + store.getCodigo());
        }

        store.setNome(store.getNome().trim());
        store.setDescricao(store.getDescricao().trim());
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(null);

        Store saved = storeRepository.save(store);
        auditLogService.record(new AuditLogEntry(
            AuditAction.CREATE,
            AuditResourceType.STORE,
            saved.getId(),
            "Loja criada",
            storeMetadata(saved)
        ));
        return saved;
    }

    @Override
    public Store updateStore(String id, Store updatedStore) {
        Optional<Store> existing = storeRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Loja não encontrada: " + id);
        }

        Store current = existing.get();

        if (updatedStore.getCodigo() != null) {
            Optional<Store> byCodigo = storeRepository.findByCodigo(updatedStore.getCodigo());
            if (byCodigo.isPresent() && !byCodigo.get().getId().equals(id)) {
                throw new IllegalStateException("Loja com código já existente: " + updatedStore.getCodigo());
            }
            current.setCodigo(updatedStore.getCodigo());
        }

        if (updatedStore.getDescricao() != null) {
            if (updatedStore.getDescricao().isBlank()) {
                throw new IllegalArgumentException("Descrição da loja é obrigatória");
            }
            current.setDescricao(updatedStore.getDescricao().trim());
        }

        if (updatedStore.getNome() != null) {
            if (updatedStore.getNome().isBlank()) {
                throw new IllegalArgumentException("Nome da loja é obrigatório");
            }
            current.setNome(updatedStore.getNome().trim());
        }

        current.setUpdatedAt(LocalDateTime.now());

        Store saved = storeRepository.save(current);
        auditLogService.record(new AuditLogEntry(
            AuditAction.UPDATE,
            AuditResourceType.STORE,
            saved.getId(),
            "Loja atualizada",
            storeMetadata(saved)
        ));
        return saved;
    }

    @Override
    public void deleteStore(String id) {
        Optional<Store> existing = storeRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Loja não encontrada: " + id);
        }
        storeRepository.deleteById(id);
        auditLogService.record(new AuditLogEntry(
            AuditAction.DELETE,
            AuditResourceType.STORE,
            id,
            "Loja removida",
            storeMetadata(existing.get())
        ));
    }

    private Map<String, Object> storeMetadata(Store store) {
        var metadata = new HashMap<String, Object>();
        putIfPresent(metadata, "codigo", store.getCodigo());
        putIfPresent(metadata, "nome", store.getNome());
        putIfPresent(metadata, "descricao", store.getDescricao());
        return metadata;
    }

    private void putIfPresent(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }

    private void validateRequiredFields(Integer codigo, String nome, String descricao) {
        if (codigo == null) {
            throw new IllegalArgumentException("Código da loja é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da loja é obrigatório");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição da loja é obrigatória");
        }
    }

    private boolean matchesNome(Store store, String nome) {
        if (nome == null || nome.isBlank()) {
            return true;
        }
        return store.getNome() != null && store.getNome().toLowerCase().contains(nome.toLowerCase());
    }

    private boolean matchesDescricao(Store store, String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return true;
        }
        return store.getDescricao() != null && store.getDescricao().toLowerCase().contains(descricao.toLowerCase());
    }
}
