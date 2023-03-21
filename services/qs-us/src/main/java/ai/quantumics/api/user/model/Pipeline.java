package ai.quantumics.api.user.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "qsp_pipeline")
public class Pipeline {
	
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private int pipelineId;

	 private String pipelineName;
	 private String pipelineType;
	 private int pipelineStatus;
	 
	 /*@OneToMany(
		      cascade = CascadeType.ALL,
		      orphanRemoval = true,
		      mappedBy = "pipeline"
		  )
	 private List<ConnectorDetails> connectors = new ArrayList<>();*/
	 
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
	
	 
	 /*public Pipeline addConnector(ConnectorDetails connector) {
		    connector.setPipeline(this);
		    connectors.add(connector);
		    return this;
	}*/

}
