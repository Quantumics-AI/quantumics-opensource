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
@Table(name = "qs_app_system_status")
public class AppSystemStatus {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int statusId;
  private String type;
  private int outages;
  private int uptime;
  private int fromDays;
  private Date creationDate;
  private String createdBy;
  private Date modifiedDate;
  private String modifiedBy;
}
