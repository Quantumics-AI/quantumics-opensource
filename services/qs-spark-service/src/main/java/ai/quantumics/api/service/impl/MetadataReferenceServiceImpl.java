package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Service;

import ai.quantumics.api.model.MetadataReference;
import ai.quantumics.api.repo.MetadataReferenceRepository;
import ai.quantumics.api.service.MetadataReferenceService;

@Service
public class MetadataReferenceServiceImpl implements MetadataReferenceService{

	private final MetadataReferenceRepository metadataReferenceRepository;
	
	public MetadataReferenceServiceImpl(MetadataReferenceRepository metadataReferenceRepository) {
	    this.metadataReferenceRepository = metadataReferenceRepository;
	}

	@Override
	public List<MetadataReference> getMetadataRefBySourceType(String sourceType) throws SQLException {
		return metadataReferenceRepository.findBySourceTypeAndActiveTrue(sourceType);
	}
}
