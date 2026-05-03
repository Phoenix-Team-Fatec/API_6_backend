package team.phoenix.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

class HttpCommissionAiClientTest {

    @Test
    void calculate_sendsLocalDatesAsIsoStrings() throws Exception {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        HttpServer server = startServer(capturedBody);
        try {
            var client = new HttpCommissionAiClient("http://127.0.0.1:" + server.getAddress().getPort());
            var request = new AiCommissionRequest(
                List.of(),
                List.of(new AiFuncionario(
                    "M1",
                    10,
                    "PRETO",
                    "35",
                    "LOJA-35",
                    LocalDate.of(2013, 8, 8),
                    LocalDate.of(2025, 7, 15),
                    100,
                    "VENDEDOR LOJA"
                )),
                List.of(),
                List.of()
            );

            client.calculate(request, 2025, 7);

            assertThat(capturedBody.get()).contains("\"data_admissao\":\"2013-08-08\"");
            assertThat(capturedBody.get()).contains("\"data_demissao\":\"2025-07-15\"");
            assertThat(capturedBody.get()).doesNotContain("\"data_admissao\":[2013,8,8]");
        } finally {
            server.stop(0);
        }
    }

    private HttpServer startServer(AtomicReference<String> capturedBody) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/commission-algorithm", exchange -> {
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes()));
            byte[] response = "[]".getBytes();
            exchange.getResponseHeaders().put("Content-Type", List.of("application/json"));
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        return server;
    }
}
