package team.phoenix.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team.phoenix.backend.domain.model.Usuario;
import team.phoenix.backend.domain.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.Collection;

// Serviço que implementa UserDetailsService do Spring Security
// Responsável por carregar informações do usuário do banco de dados
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carrega os detalhes do usuário pelo email (username)
     * Retorna um UserDetails do Spring Security com as permissões mapeadas
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .authorities(mapearAuthorities(usuario))
                .accountLocked(!usuario.getAtivo())
                .build();
    }

    /**
     * Mapeia o papel do usuário para authorities do Spring Security
     * ADMIN -> ROLE_ADMIN
     * USER -> ROLE_USER
     */
    private Collection<? extends GrantedAuthority> mapearAuthorities(Usuario usuario) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        if (usuario.getPapel() != null) {
            String papel = usuario.getPapel().toUpperCase();
            if (!papel.startsWith("ROLE_")) {
                papel = "ROLE_" + papel;
            }
            authorities.add(new SimpleGrantedAuthority(papel));
        }
        
        return authorities;
    }
}
