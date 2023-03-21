package ai.quantumics.api.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.quantumics.api.model.MetadataReference;

@Repository
public interface MetadataReferenceRepository extends JpaRepository<MetadataReference, Integer>{
	
	public  List<MetadataReference> findBySourceTypeAndActiveTrue(String sourceType);

}
