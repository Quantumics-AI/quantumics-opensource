package ai.quantumics.api.user.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "qs_metadata_reference")
public class MetadataReference {
	
	  @Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY)
	  private long refId;
	  
	  private String sourceColumnnameType;
	  
	  private String destinationColumnnameType;
	  
	  private String sourceType;
	  
	  private String targetType;
	  
	  private boolean active;
	  
	  private Date createdDate;
	  
	  private int createdBy;
	  
	  private int modifiedBy;
	  
	  private Date modifiedDate;

}
