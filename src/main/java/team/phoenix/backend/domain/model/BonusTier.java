package team.phoenix.backend.domain.model;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BonusTier {
    private Double minValue;
    private Double maxValue;    // null = no upper bound
    private Double bonusAmount;
}
