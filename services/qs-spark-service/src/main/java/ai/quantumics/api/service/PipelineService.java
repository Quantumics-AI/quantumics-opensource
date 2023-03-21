package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;

import ai.quantumics.api.model.Pipeline;

public interface PipelineService {
	
	Pipeline save(Pipeline pipeline) throws SQLException;
	
	Pipeline getPipelineById(int pipelineId) throws SQLException;
	
	Pipeline getPipelineByName(String pipelineName) throws SQLException;
	
	boolean existsPipelineName(String pipelineName) throws SQLException;
	
	List<Pipeline> getAllPipelines() throws SQLException;
	
}
