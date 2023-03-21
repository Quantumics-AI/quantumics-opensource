package ai.quantumics.api.model;

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
@Table(name = "qs_app_incidents")
public class AppIncidents {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int incidentId;
  private String incidentDesc;
  private Date creationDate;
  private String createdBy;
  private Date modifiedDate;
  private String modifiedBy;
  
}
