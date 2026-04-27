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

import team.phoenix.backend.domain.model.Brand;
import team.phoenix.backend.service.BrandService;

@WebMvcTest(BrandController.class)
class BrandControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean BrandService brandService;

    @Test void listBrands_noFilter_returnsOk() throws Exception {
        var brand = Brand.builder()
            .id("1")
            .codigo(10)
            .nome("PRETO")
            .descricao("PRETO")
            .createdAt(LocalDateTime.now())
            .build();
        when(brandService.listBrands(null, null, null)).thenReturn(List.of(brand));

        mockMvc.perform(get("/api/brands"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value(10))
            .andExpect(jsonPath("$[0].nome").value("PRETO"))
            .andExpect(jsonPath("$[0].descricao").value("PRETO"));
    }

    @Test void listBrands_withCodigoFilter_returnsOk() throws Exception {
        var brand = Brand.builder().id("1").codigo(10).nome("PRETO").descricao("PRETO").build();
        when(brandService.listBrands(10, null, null)).thenReturn(List.of(brand));

        mockMvc.perform(get("/api/brands").param("codigo", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value(10));
    }

    @Test void listBrands_withNomeFilter_returnsOk() throws Exception {
        var brand = Brand.builder().id("1").codigo(10).nome("PRETO").descricao("PRETO").build();
        when(brandService.listBrands(null, "pre", null)).thenReturn(List.of(brand));

        mockMvc.perform(get("/api/brands").param("nome", "pre"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("PRETO"));
    }

    @Test void getBrand_found_returnsOk() throws Exception {
        var brand = Brand.builder().id("1").codigo(10).nome("PRETO").descricao("PRETO").build();
        when(brandService.getBrandById("1")).thenReturn(Optional.of(brand));

        mockMvc.perform(get("/api/brands/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"));
    }

    @Test void getBrand_notFound_returns404() throws Exception {
        when(brandService.getBrandById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/brands/999"))
            .andExpect(status().isNotFound());
    }

    @Test void createBrand_withValidData_returnsCreated() throws Exception {
        var created = Brand.builder().id("1").codigo(10).nome("PRETO").descricao("PRETO").build();
        when(brandService.createBrand(any())).thenReturn(created);

        mockMvc.perform(post("/api/brands")
                .contentType("application/json")
            .content("{\"codigo\":10,\"nome\":\"PRETO\",\"descricao\":\"PRETO\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigo").value(10));
    }

    @Test void createBrand_withBusinessError_returns400() throws Exception {
        when(brandService.createBrand(any()))
            .thenThrow(new IllegalStateException("Marca com código já existente: 10"));

        mockMvc.perform(post("/api/brands")
                .contentType("application/json")
            .content("{\"codigo\":10,\"nome\":\"PRETO\",\"descricao\":\"PRETO\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Erro ao criar marca: Marca com código já existente: 10"));
    }

    @Test void updateBrand_withValidData_returnsOk() throws Exception {
        var updated = Brand.builder().id("1").codigo(20).nome("CINZA").descricao("CINZA").build();
        when(brandService.updateBrand(anyString(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/brands/1")
                .contentType("application/json")
            .content("{\"codigo\":20,\"nome\":\"CINZA\",\"descricao\":\"CINZA\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value(20));
    }

    @Test void deleteBrand_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/brands/1"))
            .andExpect(status().isNoContent());

        verify(brandService).deleteBrand("1");
    }
}
