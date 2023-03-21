package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "qs_Userprojects")
public class QsUserProjects {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  
  private int projectId;
  private int userId;
  private Date creationDate;
  private String createdBy;
  private Date modifiedDate;
  private String modifiedBy;
}
