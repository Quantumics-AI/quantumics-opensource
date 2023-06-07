package ai.quantumics.api.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "qsp_pipeline")
public class Pipeline {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int pipelineId;

	private String pipelineName;
	private String pipelineType;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinTable(name = "qsp_pipeline_connector_details",
			joinColumns = { @JoinColumn(name = "pipelineId", referencedColumnName = "pipelineId") },
			inverseJoinColumns = { @JoinColumn(name = "connectorId", referencedColumnName = "connectorId") })
	private ConnectorDetails connectorDetails;

	private boolean active;
	private boolean isPublished;
	private Date createdDate;
	private Date modifiedDate;
	private String createdBy;
	private String modifiedBy;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "pipelineId", referencedColumnName = "pipelineId", insertable = false, updatable = false)
	private List<DatasetSchema> datasetSchema;

	@Transient
	private Date executionDate;

	@Transient
	private String transactionStatus;


}
