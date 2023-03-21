package ai.quantumics.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.AppIncidents;

@Repository
public interface AppIncidentsRepository extends JpaRepository<AppIncidents, Integer> {
}
