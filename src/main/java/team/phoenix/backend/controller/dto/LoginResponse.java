package team.phoenix.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para resposta de login (retorna token JWT)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tipo; // Sempre "Bearer"
    private String id;
    private String nome;
    private String email;
    private String papel;
}
