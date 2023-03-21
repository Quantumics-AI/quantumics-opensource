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
@Table(name = "qs_notifications")
public class QsNotifications {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int notificationId;
  
  private String notificationMsg;
  private boolean adminMsg;
  private boolean notificationRead;
  private Date creationDate;
  private int projectId;
  private int userId;
  
}
