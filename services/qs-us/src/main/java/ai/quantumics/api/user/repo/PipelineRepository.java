package ai.quantumics.api.user.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.quantumics.api.user.model.Pipeline;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Integer> {
	
	boolean existsByPipelineNameIgnoreCaseAndActiveTrue(String pipelineName);
	
	Optional<Pipeline> findByPipelineNameAndActiveTrue(String pipelineName);
	
	Pipeline findByPipelineIdAndActiveTrue(int pipelineId);
	
	List<Pipeline> findByActiveTrue();

}
