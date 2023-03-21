package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import ai.quantumics.api.model.DatasetSchema;

public interface DatasetSchemaService {

	DatasetSchema save(DatasetSchema datasetSchema) throws SQLException;
	
	Optional<DatasetSchema> getLatestDatasetSchema(int folderId) throws SQLException;
	
	List<DatasetSchema> getDatasetSchemaByPipelineId(int pipelineId) throws SQLException;
	
	List<DatasetSchema> getActiveDatasetSchemaByPipelineId(int pipelineId,boolean active) throws SQLException;
	
}
