package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;

import ai.quantumics.api.model.PipelineTranscationDetails;

public interface PipelineTranscationService {

	PipelineTranscationDetails savePipelineTranscations(final PipelineTranscationDetails transcation) throws SQLException;
	
	List<PipelineTranscationDetails> getByPipelineTranscations(int pipelineId) throws SQLException;
}
