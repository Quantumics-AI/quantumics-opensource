package ai.quantumics.api.model;

import java.util.Date;

import javax.persistence.Column;
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
@Table(name = "qsp_eng_flow_data")
public class EngFlowData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int engFlowDataId;
	
	private int projectId;
	private int engFlowId;

	@Column(columnDefinition = "TEXT")
	private String engFlowData;
	
	private boolean active;
	private Date creationDate;
}
