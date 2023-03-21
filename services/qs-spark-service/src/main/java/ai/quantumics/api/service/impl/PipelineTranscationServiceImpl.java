package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Service;

import ai.quantumics.api.model.PipelineTranscationDetails;
import ai.quantumics.api.repo.PipelineTranscationRepository;
import ai.quantumics.api.service.PipelineTranscationService;

@Service
public class PipelineTranscationServiceImpl implements PipelineTranscationService {
	
	private final PipelineTranscationRepository pipelineTranscationRepository;
	  
	  public PipelineTranscationServiceImpl(PipelineTranscationRepository pipelineTranscationRepository) {
	    this.pipelineTranscationRepository = pipelineTranscationRepository;
	  }

	@Override
	public PipelineTranscationDetails savePipelineTranscations(final PipelineTranscationDetails transcation) throws SQLException {
		return pipelineTranscationRepository.save(transcation);
	}

	@Override
	public List<PipelineTranscationDetails> getByPipelineTranscations(int pipelineId) throws SQLException {
		return pipelineTranscationRepository.findByPipelineId(pipelineId);
	}

}
