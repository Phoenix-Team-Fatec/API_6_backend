package team.phoenix.backend.service;

import team.phoenix.backend.domain.model.BusinessRule;
import java.util.List;

public interface BusinessRuleService {
    BusinessRule create(String rawText);
    BusinessRule create(String rawText, String yearMonth);
    List<BusinessRule> listAll();
}
