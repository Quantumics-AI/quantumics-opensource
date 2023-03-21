package ai.quantumics.api.user.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "qsp_dataset_schema")
public class DatasetSchema {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int datasetschemaId;

	private int folderId;
	private int pipelineId;
	private String sqlScript;
	private String sqlType;
	private String objectName;
	private String objectType;
	private String datasetProperties;
	private String schemaName;

	private Date createdDate;
	private Date modifiedDate;
	private int createdBy;
	private int modifiedBy;
	private boolean active;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="folderId", referencedColumnName = "folderId", insertable = false, updatable = false)
    private QsFolders qsFolders;
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "datasetschemaId", referencedColumnName = "datasetschemaId", insertable = false, updatable = false)
    private DatasetMetadata datasetMetadata;

}
