package team.phoenix.backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador para teste básico de conectividade
@RestController
@RequestMapping("/hello")
@CrossOrigin
public class HelloController {
    
    // GET /hello - Retorna mensagem de teste
    // Sem parâmetros
    // Retorna: String simples como teste de conectividade
    @GetMapping
    public String hello() {
        return "Hello, Spring Boot!";
    }

}
