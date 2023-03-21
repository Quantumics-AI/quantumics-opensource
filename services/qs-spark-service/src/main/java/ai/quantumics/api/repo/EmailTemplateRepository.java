package ai.quantumics.api.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.EmailTemplate;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Integer> {
  
  Optional<EmailTemplate> findByTemplateId(int templateId);
  
  Optional<EmailTemplate> findByAction(String action);
}
