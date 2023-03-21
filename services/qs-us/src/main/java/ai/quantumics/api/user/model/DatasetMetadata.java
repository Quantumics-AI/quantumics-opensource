package ai.quantumics.api.user.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "qsp_dataset_metadata")
public class DatasetMetadata {
	
	@Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private int datasetmetadataId;
	
	 private int datasetschemaId;
	 
	 @Column(columnDefinition = "TEXT")
	 private String sourceMetadata;
	 
	 @Column(columnDefinition = "TEXT")
	 private String uiMetadata;
	 
	 @Column(columnDefinition = "TEXT")
	 private String targetMetadata;
	 
	 private String version;
	 
	 private Date createdDate;
	 private Date modifiedDate;
	 private int createdBy;
	 private int modifiedBy;

}
