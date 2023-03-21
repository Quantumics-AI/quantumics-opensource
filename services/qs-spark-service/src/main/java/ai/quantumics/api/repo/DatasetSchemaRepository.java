package ai.quantumics.api.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.quantumics.api.model.DatasetSchema;

@Repository
public interface DatasetSchemaRepository extends JpaRepository<DatasetSchema, Integer>{
	
	Optional<DatasetSchema> findTopByFolderIdOrderByDatasetschemaIdDesc(int folderId);
	
	List<DatasetSchema> findByPipelineId(int pipelineId);
	
	List<DatasetSchema> findByPipelineIdAndActive(int pipelineId,boolean active);
	
}
