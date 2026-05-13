package team.phoenix.backend.commission.application;

import java.util.List;

public interface CommissionAiClient {
    List<AiCommissionResult> calculate(AiCommissionRequest request, int year, int month);
}
