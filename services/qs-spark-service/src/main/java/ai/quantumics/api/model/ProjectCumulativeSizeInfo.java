package ai.quantumics.api.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "qs_project_cumulative_size_info")
public class ProjectCumulativeSizeInfo {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  
  private int projectId;
  private int userId;
  private long cumulativeSize;

}
