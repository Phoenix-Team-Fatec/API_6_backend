package team.phoenix.backend.service;

import java.util.List;

public interface CommissionAiClient {
    List<AiCommissionResult> calculate(AiCommissionRequest request, int year, int month);
}
