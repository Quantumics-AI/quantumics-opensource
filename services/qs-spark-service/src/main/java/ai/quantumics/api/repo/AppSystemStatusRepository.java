package ai.quantumics.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.AppSystemStatus;

@Repository
public interface AppSystemStatusRepository extends JpaRepository<AppSystemStatus, Integer> {
  
}
