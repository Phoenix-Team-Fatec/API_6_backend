package team.phoenix.backend.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import team.phoenix.backend.domain.model.BusinessRule;
import java.util.List;

// Repositório para acesso a regras de negócio no MongoDB
public interface BusinessRuleRepository extends MongoRepository<BusinessRule, String> {
    List<BusinessRule> findAllByOrderByCreatedAtDesc();
}
