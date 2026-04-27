package team.phoenix.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import team.phoenix.backend.domain.model.Store;
import team.phoenix.backend.service.StoreService;

@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean StoreService storeService;

    @Test void listStores_noFilter_returnsOk() throws Exception {
        var store = Store.builder()
            .id("1")
            .codigo(10)
            .nome("LOJA A")
            .descricao("Centro")
            .createdAt(LocalDateTime.now())
            .build();
        when(storeService.listStores(null, null, null)).thenReturn(List.of(store));

        mockMvc.perform(get("/api/stores"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value(10))
            .andExpect(jsonPath("$[0].nome").value("LOJA A"))
            .andExpect(jsonPath("$[0].descricao").value("Centro"));
    }

    @Test void listStores_withCodigoFilter_returnsOk() throws Exception {
        var store = Store.builder().id("1").codigo(10).nome("LOJA A").descricao("Centro").build();
        when(storeService.listStores(10, null, null)).thenReturn(List.of(store));

        mockMvc.perform(get("/api/stores").param("codigo", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value(10));
    }

    @Test void listStores_withNomeFilter_returnsOk() throws Exception {
        var store = Store.builder().id("1").codigo(10).nome("LOJA A").descricao("Centro").build();
        when(storeService.listStores(null, "loja", null)).thenReturn(List.of(store));

        mockMvc.perform(get("/api/stores").param("nome", "loja"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("LOJA A"));
    }

    @Test void getStore_found_returnsOk() throws Exception {
        var store = Store.builder().id("1").codigo(10).nome("LOJA A").descricao("Centro").build();
        when(storeService.getStoreById("1")).thenReturn(Optional.of(store));

        mockMvc.perform(get("/api/stores/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"));
    }

    @Test void getStore_notFound_returns404() throws Exception {
        when(storeService.getStoreById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/stores/999"))
            .andExpect(status().isNotFound());
    }

    @Test void createStore_withValidData_returnsCreated() throws Exception {
        var created = Store.builder().id("1").codigo(10).nome("LOJA A").descricao("Centro").build();
        when(storeService.createStore(any())).thenReturn(created);

        mockMvc.perform(post("/api/stores")
                .contentType("application/json")
            .content("{\"codigo\":10,\"nome\":\"LOJA A\",\"descricao\":\"Centro\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigo").value(10));
    }

    @Test void createStore_withBusinessError_returns400() throws Exception {
        when(storeService.createStore(any()))
            .thenThrow(new IllegalStateException("Loja com código já existente: 10"));

        mockMvc.perform(post("/api/stores")
                .contentType("application/json")
            .content("{\"codigo\":10,\"nome\":\"LOJA A\",\"descricao\":\"Centro\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Erro ao criar loja: Loja com código já existente: 10"));
    }

    @Test void updateStore_withValidData_returnsOk() throws Exception {
        var updated = Store.builder().id("1").codigo(20).nome("LOJA B").descricao("Shopping").build();
        when(storeService.updateStore(anyString(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/stores/1")
                .contentType("application/json")
            .content("{\"codigo\":20,\"nome\":\"LOJA B\",\"descricao\":\"Shopping\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value(20));
    }

    @Test void deleteStore_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/stores/1"))
            .andExpect(status().isNoContent());

        verify(storeService).deleteStore("1");
    }
}
