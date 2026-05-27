package team.phoenix.backend.commission.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Representa uma etapa do cálculo de comissão com rastreabilidade completa.
 * Cada etapa registra entrada, lógica aplicada e saída.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaCalculo {

    /**
     * Número sequencial da etapa (1, 2, 3, ...)
     */
    private int numero;

    /**
     * Seção agrupadora (ex: "Consolidação", "Percentual", "Proporcional", "Intercorrências")
     */
    private String secao;

    /**
     * Descrição legível da etapa
     */
    private String descricao;

    /**
     * Valores que entraram nesta etapa
     */
    private Map<String, Object> entrada;

    /**
     * Resultado/saída desta etapa
     */
    private Map<String, Object> saida;

    /**
     * Resumo da lógica aplicada
     */
    @JsonProperty("logica_aplicada")
    private String logicaAplicada;

    /**
     * Condição que ativou a etapa (opcional)
     */
    private String condicao;

    /**
     * Momento em que a etapa foi executada
     */
    private LocalDateTime timestamp;
}
