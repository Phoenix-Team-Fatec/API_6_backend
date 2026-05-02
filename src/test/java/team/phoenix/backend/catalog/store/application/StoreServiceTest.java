package team.phoenix.backend.catalog.store.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team.phoenix.backend.domain.model.Store;
import team.phoenix.backend.domain.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock StoreRepository storeRepository;
    @InjectMocks StoreServiceImpl service;

    @Test void listStores_noFilter_returnsAll() {
        var s1 = Store.builder().id("1").codigo(10).nome("LOJA A").descricao("Centro").build();
        var s2 = Store.builder().id("2").codigo(20).nome("LOJA B").descricao("Shopping").build();
        when(storeRepository.findAll()).thenReturn(List.of(s1, s2));

        assertThat(service.listStores(null, null, null)).containsExactly(s1, s2);
    }

    @Test void listStores_filterByCodigo_returnsOne() {
        var s = Store.builder().id("1").codigo(10).nome("LOJA A").descricao("Centro").build();
        when(storeRepository.findByCodigo(10)).thenReturn(Optional.of(s));

        assertThat(service.listStores(10, null, null)).containsExactly(s);
    }

    @Test void listStores_filterByNome_returnsFiltered() {
        var s = Store.builder().id("1").codigo(10).nome("LOJA A").descricao("Centro").build();
        when(storeRepository.findByNomeContainingIgnoreCase("loja")).thenReturn(List.of(s));

        assertThat(service.listStores(null, "loja", null)).containsExactly(s);
    }

    @Test void createStore_withValidData_setsAuditAndSaves() {
        var input = Store.builder().codigo(10).nome(" LOJA A ").descricao(" Centro ").build();
        when(storeRepository.findByCodigo(10)).thenReturn(Optional.empty());
        when(storeRepository.save(input)).thenReturn(input);

        var created = service.createStore(input);

        assertThat(created.getNome()).isEqualTo("LOJA A");
        assertThat(created.getDescricao()).isEqualTo("Centro");
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNull();
        verify(storeRepository).save(input);
    }

    @Test void createStore_withDuplicatedCodigo_throwsConflict() {
        when(storeRepository.findByCodigo(10))
            .thenReturn(Optional.of(Store.builder().id("1").codigo(10).nome("LOJA A").descricao("Centro").build()));

        assertThatThrownBy(() -> service.createStore(Store.builder().codigo(10).nome("LOJA A").descricao("Centro").build()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Loja com código já existente: 10");
    }

    @Test void updateStore_notFound_throwsNotFound() {
        when(storeRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStore("999", Store.builder().nome("NOVA").descricao("Nova descrição").build()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Loja não encontrada: 999");
    }

    @Test void updateStore_updatesOnlySentFields() {
        var current = Store.builder()
            .id("1")
            .codigo(10)
            .nome("LOJA A")
            .descricao("Centro")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
        when(storeRepository.findById("1")).thenReturn(Optional.of(current));
        when(storeRepository.findByCodigo(20)).thenReturn(Optional.empty());
        when(storeRepository.save(current)).thenReturn(current);

        var updated = service.updateStore("1", Store.builder().codigo(20).nome(" LOJA B ").descricao(" Shopping ").build());

        assertThat(updated.getCodigo()).isEqualTo(20);
        assertThat(updated.getNome()).isEqualTo("LOJA B");
        assertThat(updated.getDescricao()).isEqualTo("Shopping");
        assertThat(updated.getUpdatedAt()).isNotNull();
        verify(storeRepository).save(current);
    }

    @Test void deleteStore_notFound_throwsNotFound() {
        when(storeRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteStore("999"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Loja não encontrada: 999");
        verify(storeRepository, never()).deleteById("999");
    }
}
