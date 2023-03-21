package ai.quantumics.api.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.quantumics.api.model.PipelineTranscationDetails;

@Repository
public interface PipelineTranscationRepository extends JpaRepository<PipelineTranscationDetails, Integer>{
	
	List<PipelineTranscationDetails> findByPipelineId(int pipelineId);

}
