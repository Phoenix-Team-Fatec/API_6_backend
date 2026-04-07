package team.phoenix.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

// Aplicação principal Spring Boot que inicia o servidor da API
@SpringBootApplication
public class BackendApplication {

	// Ponto de entrada da aplicação
	// Inicia o contexto Spring e executa a aplicação
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
