package ai.quantumics.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.QsUserConsent;

@Repository
public interface UserConsentRepository extends JpaRepository<QsUserConsent, Integer>{
  
}
