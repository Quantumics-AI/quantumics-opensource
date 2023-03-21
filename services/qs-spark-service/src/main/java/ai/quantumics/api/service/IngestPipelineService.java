package ai.quantumics.api.service;

import java.util.Map;

import ai.quantumics.api.model.Pipeline;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFolders;
import ai.quantumics.api.model.QsUserV2;
import ai.quantumics.api.req.ConnectorProperties;
import ai.quantumics.api.req.DataBaseRequest;

public interface IngestPipelineService {
	
	public boolean savePipeline(DataBaseRequest request, QsUserV2 userObj);
	
	public QsFolders savePipelineFolderInfo(Projects project,Pipeline pipeline,DataBaseRequest request, QsUserV2 userObj, String tableName, String schema, String sqlquery,ConnectorProperties properties) throws Exception;
	
	public boolean updatePipelineConnector(DataBaseRequest request, QsUserV2 userObj);
	
	public Map<String, Object> executePipelineService(Projects project, QsUserV2 userObj, Pipeline pipeline);
	
	public Map<Integer, Pipeline> getAllPipelinesByFolder();

}
