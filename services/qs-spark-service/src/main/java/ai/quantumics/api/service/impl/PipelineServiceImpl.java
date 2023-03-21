package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ai.quantumics.api.model.Pipeline;
import ai.quantumics.api.repo.PipelineRepository;
import ai.quantumics.api.service.PipelineService;

@Service
public class PipelineServiceImpl implements PipelineService{

	private PipelineRepository pipelineRepository;

	public PipelineServiceImpl(PipelineRepository pipelineRepository) {
		this.pipelineRepository = pipelineRepository;
	}

	public Pipeline save(final Pipeline pipeline) throws SQLException {
		return pipelineRepository.save(pipeline);
	}
	
	public Pipeline getPipelineById(int pipelineId) throws SQLException{
		return pipelineRepository.findByPipelineIdAndActiveTrue(pipelineId);
	}

	
	public boolean existsPipelineName(String pipelineName) throws SQLException {
		return pipelineRepository.existsByPipelineNameIgnoreCaseAndActiveTrue(pipelineName);
	}
	
	
	public Pipeline getPipelineByName(String pipelineName) throws SQLException {
		Optional<Pipeline> pipeline =  pipelineRepository.findByPipelineNameAndActiveTrue(pipelineName);
		if(pipeline.isPresent()) return pipeline.get();
		else return null;
	}
	
    @Override
    public List<Pipeline> getAllPipelines() throws SQLException {
		return pipelineRepository.findByActiveTrue();
	}

}
