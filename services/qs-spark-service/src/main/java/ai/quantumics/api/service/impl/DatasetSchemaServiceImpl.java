package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ai.quantumics.api.model.DatasetSchema;
import ai.quantumics.api.repo.DatasetSchemaRepository;
import ai.quantumics.api.service.DatasetSchemaService;

@Service
public class DatasetSchemaServiceImpl implements DatasetSchemaService{
	
 private final DatasetSchemaRepository datasetSchemaRepo;
 
 public DatasetSchemaServiceImpl(DatasetSchemaRepository datasetSchemaRepository) {
	  this.datasetSchemaRepo = datasetSchemaRepository;
 }

@Override
public DatasetSchema save(DatasetSchema datasetSchema) throws SQLException {
	return datasetSchemaRepo.save(datasetSchema);
}

@Override
public Optional<DatasetSchema> getLatestDatasetSchema(int folderId) throws SQLException{
	return datasetSchemaRepo.findTopByFolderIdOrderByDatasetschemaIdDesc(folderId);
}

@Override
public List<DatasetSchema> getDatasetSchemaByPipelineId(int pipelineId) throws SQLException {
	return datasetSchemaRepo.findByPipelineId(pipelineId);
}

@Override
public List<DatasetSchema> getActiveDatasetSchemaByPipelineId(int pipelineId,boolean active) throws SQLException {
	return datasetSchemaRepo.findByPipelineIdAndActive(pipelineId,active);
}
 
}
