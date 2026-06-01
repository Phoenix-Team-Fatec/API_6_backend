package team.phoenix.backend;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import team.phoenix.backend.config.security.JwtService;
import team.phoenix.backend.config.security.UserDetailsServiceImpl;

public abstract class WebMvcSecurityMocks {

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected UserDetailsServiceImpl userDetailsService;
}
