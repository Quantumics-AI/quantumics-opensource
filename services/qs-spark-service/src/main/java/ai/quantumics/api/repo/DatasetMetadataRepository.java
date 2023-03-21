package ai.quantumics.api.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.quantumics.api.model.DatasetMetadata;

@Repository
public interface DatasetMetadataRepository extends JpaRepository<DatasetMetadata, Integer>{
	
	Optional<DatasetMetadata> findByDatasetschemaId(int datasetschemaId);

}
