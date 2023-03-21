package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ai.quantumics.api.model.DatasetMetadata;
import ai.quantumics.api.repo.DatasetMetadataRepository;
import ai.quantumics.api.service.DatasetMetadataService;

@Service
public class DatasetMetadataServiceImpl implements DatasetMetadataService{
	
	  private final DatasetMetadataRepository datasetMetadataRepo;
	  
	  public DatasetMetadataServiceImpl(DatasetMetadataRepository datasetMetadataRepo) {
	    this.datasetMetadataRepo = datasetMetadataRepo;
	  }

	@Override
	public Optional<DatasetMetadata> getByDatasetMetadata(int datasetschemaId) {
		return datasetMetadataRepo.findByDatasetschemaId(datasetschemaId);
	}

	@Override
	public DatasetMetadata save(DatasetMetadata datasetMetadata) throws SQLException {
		return datasetMetadataRepo.save(datasetMetadata);
	}

}
