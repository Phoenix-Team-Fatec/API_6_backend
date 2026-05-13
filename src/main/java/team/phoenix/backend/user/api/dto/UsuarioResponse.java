package team.phoenix.backend.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.phoenix.backend.domain.model.Usuario;
import java.time.LocalDateTime;

// DTO para resposta de usuário (sem expor a senha)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private String id;
    private String nome;
    private String email;
    private String papel;
    private Boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public static UsuarioResponse from(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .papel(usuario.getPapel())
                .ativo(usuario.getAtivo())
                .criadoEm(usuario.getCriadoEm())
                .atualizadoEm(usuario.getAtualizadoEm())
                .build();
    }
}
