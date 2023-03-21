package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "qsp_redash_view_info")
@Data
public class RedashViewInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int redashViewId;
  
  private String schemaName;
  private String viewName;
  private boolean active;
  private Date creationDate;
  
}
