package team.phoenix.backend.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import team.phoenix.backend.domain.model.Funcionario;
import team.phoenix.backend.service.FuncionarioService;

@WebMvcTest(FuncionarioController.class)
class FuncionarioControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FuncionarioService funcionarioService;

    @Test
    void listActive_returnsOk() throws Exception {
        var funcionario = Funcionario.builder()
            .id(new ObjectId())
            .matricula("MAT-001")
            .type("CLT")
            .yearMonth(new Date())
            .build();

        when(funcionarioService.listActive()).thenReturn(List.of(funcionario));

        mockMvc.perform(get("/api/funcionarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].matricula").value("MAT-001"))
            .andExpect(jsonPath("$[0].type").value("CLT"));
    }

    @Test
    void softDelete_returnsNoContent() throws Exception {
        var id = new ObjectId().toHexString();

        mockMvc.perform(delete("/api/funcionarios/{id}", id))
            .andExpect(status().isNoContent());

        verify(funcionarioService).softDelete(id);
    }

    @Test
    void softDelete_whenNotFound_returns404() throws Exception {
        var id = new ObjectId().toHexString();
        doThrow(new IllegalArgumentException("Funcionario not found"))
            .when(funcionarioService).softDelete(id);

        mockMvc.perform(delete("/api/funcionarios/{id}", id))
            .andExpect(status().isNotFound());
    }
}