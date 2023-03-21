package ai.quantumics.api.model;

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
@Table(name = "qsp_eng_flow_datalineage")
public class EngFlowDatalineage {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int lineageId;
  
  private int engFlowId;
  private int eventId;
  private int eventMappingId;
  private String eventType;
  private String joinType;
  private int recordCount;
  private String attributes;
  
}
