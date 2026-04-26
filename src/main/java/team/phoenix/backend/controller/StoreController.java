package team.phoenix.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import team.phoenix.backend.domain.model.Store;
import team.phoenix.backend.service.StoreService;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<List<StoreResponse>> listStores(
            @RequestParam(required = false) Integer codigo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String descricao) {
        var stores = storeService.listStores(codigo, nome, descricao)
            .stream().map(StoreResponse::from).toList();
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStore(@PathVariable String id) {
        var store = storeService.getStoreById(id);
        if (store.isPresent()) {
            return ResponseEntity.ok(StoreResponse.from(store.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createStore(@RequestBody CreateStoreRequest req) {
        try {
            var store = Store.builder()
                .codigo(req.codigo())
                .nome(req.nome())
                .descricao(req.descricao())
                .build();

            var created = storeService.createStore(store);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(StoreResponse.from(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar loja: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStore(@PathVariable String id, @RequestBody CreateStoreRequest req) {
        try {
            var updated = Store.builder()
                .codigo(req.codigo())
                .nome(req.nome())
                .descricao(req.descricao())
                .build();

            var store = storeService.updateStore(id, updated);
            return ResponseEntity.ok(StoreResponse.from(store));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar loja: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStore(@PathVariable String id) {
        try {
            storeService.deleteStore(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar loja: " + e.getMessage());
        }
    }
}
