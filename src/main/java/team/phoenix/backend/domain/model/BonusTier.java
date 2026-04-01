package team.phoenix.backend.domain.model;

import lombok.*;

// Define uma faixa de vendas para cálculo de bônus (ex: R$40k-50k = R$3500)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BonusTier {
    private Double minValue;
    private Double maxValue;    // null = no upper bound
    private Double bonusAmount;
}
