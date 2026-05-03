package team.phoenix.backend.audit.domain;

// Contrato para obter o usuario/ator responsavel pela operacao atual.
public interface AuditActorProvider {
    /**
     * Retorna o ator da requisicao atual ou um ator de sistema quando nao houver contexto HTTP.
     */
    AuditActor getCurrentActor();
}
