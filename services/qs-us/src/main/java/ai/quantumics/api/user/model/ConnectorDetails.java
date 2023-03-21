package ai.quantumics.api.user.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "qsp_connector_details")
public class ConnectorDetails {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private int connectorId;

 private String connectorName;
 
 //@ManyToOne
 //private Pipeline pipeline;
 
 @Column(columnDefinition = "TEXT")
 private String connectorConfig;
 
 private String connectorType;
 private int projectId;
 
 private boolean active;
 private Date createdDate;
 private Date modifiedDate;
 private int createdBy;
 private int modifiedBy;
 
}

