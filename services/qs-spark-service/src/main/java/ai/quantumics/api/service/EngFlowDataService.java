package ai.quantumics.api.service;

import java.util.Optional;

import ai.quantumics.api.model.EngFlowData;

public interface EngFlowDataService {
	
	EngFlowData save(EngFlowData engFlowData);
	
	Optional<EngFlowData> getById(int engFlowDataId);
	
	Optional<EngFlowData> getByProjectIdAndEngFlowId(int projectId, int engFlowId);
}
