package ai.quantumics.api.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import ai.quantumics.api.model.EngFlowData;
import ai.quantumics.api.repo.EngFlowDataRepository;
import ai.quantumics.api.service.EngFlowDataService;

@Service
public class EngFlowDataServiceImpl implements EngFlowDataService {
	
	private final EngFlowDataRepository engFlowDataRepository;
	
	public EngFlowDataServiceImpl(EngFlowDataRepository engFlowDataRepository) {
		this.engFlowDataRepository = engFlowDataRepository;
	}

	@Override
	public EngFlowData save(EngFlowData engFlowData) {
		return engFlowDataRepository.save(engFlowData);
	}

	@Override
	public Optional<EngFlowData> getById(int engFlowDataId) {
		return engFlowDataRepository.findByEngFlowDataId(engFlowDataId);
	}

	@Override
	public Optional<EngFlowData> getByProjectIdAndEngFlowId(int projectId, int engFlowId) {
		return engFlowDataRepository.findByProjectIdAndEngFlowId(projectId, engFlowId);
	}

}
