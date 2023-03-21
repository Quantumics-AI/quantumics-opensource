package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.Optional;

import ai.quantumics.api.model.DatasetMetadata;

public interface DatasetMetadataService {
	
	Optional<DatasetMetadata> getByDatasetMetadata(int datasetschemaId);
	
	DatasetMetadata save(DatasetMetadata datasetMetadata) throws SQLException;

}
