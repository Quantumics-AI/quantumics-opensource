package ai.quantumics.api.repo;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ai.quantumics.api.model.EngFlowData;

@Repository
public interface EngFlowDataRepository extends CrudRepository<EngFlowData, Integer>{
	
	Optional<EngFlowData> findByEngFlowDataId(int engFlowDataId);
	
	Optional<EngFlowData> findByProjectIdAndEngFlowId(int projectId, int engFlowId);
}
