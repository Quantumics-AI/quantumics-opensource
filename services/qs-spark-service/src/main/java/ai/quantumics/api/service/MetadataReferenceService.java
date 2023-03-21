package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;

import ai.quantumics.api.model.MetadataReference;

public interface MetadataReferenceService {
	
	List<MetadataReference> getMetadataRefBySourceType(String sourceType) throws SQLException;

}
