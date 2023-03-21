package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class QsUserConsent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int consentId;
  private int userId;
  private String sessionId;
  private Date sessionDate;
  private Date creationDate;
}
