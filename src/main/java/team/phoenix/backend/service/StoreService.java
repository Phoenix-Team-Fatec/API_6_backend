package team.phoenix.backend.service;

import java.util.List;
import java.util.Optional;

import team.phoenix.backend.domain.model.Store;

public interface StoreService {

    List<Store> listStores(Integer codigo, String nome, String descricao);

    Optional<Store> getStoreById(String id);

    Store createStore(Store store);

    Store updateStore(String id, Store updatedStore);

    void deleteStore(String id);
}
