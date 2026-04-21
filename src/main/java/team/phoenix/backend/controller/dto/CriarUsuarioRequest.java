package team.phoenix.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.phoenix.backend.domain.model.Usuario;

// DTO para requisição de criação/edição de usuário
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarUsuarioRequest {
    private String nome;
    private String email;
    private String senha;
    private String papel; // ADMIN ou USER
}
