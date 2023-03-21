package ai.quantumics.api.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import ai.quantumics.api.constants.PipelineStatusCode;
import lombok.Data;

@Data
@Entity
@Table(name = "qsp_pipeline_transcation_details")
public class PipelineTranscationDetails {
	
	@Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private int ptid;
	
	 private int pipelineId;
	 
	 private int pipelineStatus;
	 
	 private String pipelineLog;
	 
	 private Date executionDate;
	 
	 @Transient
	 private String transcationStatus;
	 
	 public String getTranscationStatus() {
		 return PipelineStatusCode.getEnumNameForValue(this.pipelineStatus);
	 }
	 


}
