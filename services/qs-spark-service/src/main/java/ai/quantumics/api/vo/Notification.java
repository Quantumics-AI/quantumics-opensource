package ai.quantumics.api.vo;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class Notification {
  private int notificationId;
  private String notificationMsg;
  private boolean notificationRead;
  private Date creationDate;
  private String eventType;
  private String eventTypeAction; 
  private String userName;
  private boolean active;
}
