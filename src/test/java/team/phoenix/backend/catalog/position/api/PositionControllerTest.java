package team.phoenix.backend.catalog.position.api;

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

import team.phoenix.backend.domain.model.Position;
import team.phoenix.backend.catalog.position.application.PositionService;
import team.phoenix.backend.WebMvcSecurityMocks;

@WebMvcTest(PositionController.class)
class PositionControllerTest extends WebMvcSecurityMocks {

    @Autowired MockMvc mockMvc;
    @MockitoBean PositionService positionService;

    @Test void listPositions_noFilter_returnsOk() throws Exception {
        var position = Position.builder()
            .id("1")
            .codigo(10)
            .nome("VENDEDOR")
            .descricao("Vendas")
            .createdAt(LocalDateTime.now())
            .build();
        when(positionService.listPositions(null, null, null)).thenReturn(List.of(position));

        mockMvc.perform(get("/api/positions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value(10))
            .andExpect(jsonPath("$[0].nome").value("VENDEDOR"))
            .andExpect(jsonPath("$[0].descricao").value("Vendas"));
    }

    @Test void listPositions_withCodigoFilter_returnsOk() throws Exception {
        var position = Position.builder().id("1").codigo(10).nome("VENDEDOR").descricao("Vendas").build();
        when(positionService.listPositions(10, null, null)).thenReturn(List.of(position));

        mockMvc.perform(get("/api/positions").param("codigo", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value(10));
    }

    @Test void listPositions_withNomeFilter_returnsOk() throws Exception {
        var position = Position.builder().id("1").codigo(10).nome("VENDEDOR").descricao("Vendas").build();
        when(positionService.listPositions(null, "vendedor", null)).thenReturn(List.of(position));

        mockMvc.perform(get("/api/positions").param("nome", "vendedor"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("VENDEDOR"));
    }

    @Test void getPosition_found_returnsOk() throws Exception {
        var position = Position.builder().id("1").codigo(10).nome("VENDEDOR").descricao("Vendas").build();
        when(positionService.getPositionById("1")).thenReturn(Optional.of(position));

        mockMvc.perform(get("/api/positions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"));
    }

    @Test void getPosition_notFound_returns404() throws Exception {
        when(positionService.getPositionById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/positions/999"))
            .andExpect(status().isNotFound());
    }

    @Test void createPosition_withValidData_returnsCreated() throws Exception {
        var created = Position.builder().id("1").codigo(10).nome("VENDEDOR").descricao("Vendas").build();
        when(positionService.createPosition(any())).thenReturn(created);

        mockMvc.perform(post("/api/positions")
                .contentType("application/json")
            .content("{\"codigo\":10,\"nome\":\"VENDEDOR\",\"descricao\":\"Vendas\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigo").value(10));
    }

    @Test void createPosition_withBusinessError_returns400() throws Exception {
        when(positionService.createPosition(any()))
            .thenThrow(new IllegalStateException("Cargo com código já existente: 10"));

        mockMvc.perform(post("/api/positions")
                .contentType("application/json")
            .content("{\"codigo\":10,\"nome\":\"VENDEDOR\",\"descricao\":\"Vendas\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Erro ao criar cargo: Cargo com código já existente: 10"));
    }

    @Test void updatePosition_withValidData_returnsOk() throws Exception {
        var updated = Position.builder().id("1").codigo(20).nome("GERENTE").descricao("Gestão").build();
        when(positionService.updatePosition(anyString(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/positions/1")
                .contentType("application/json")
            .content("{\"codigo\":20,\"nome\":\"GERENTE\",\"descricao\":\"Gestão\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value(20));
    }

    @Test void deletePosition_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/positions/1"))
            .andExpect(status().isNoContent());

        verify(positionService).deletePosition("1");
    }
}
