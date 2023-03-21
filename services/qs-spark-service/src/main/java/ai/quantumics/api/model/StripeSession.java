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
@Table(name = "qs_stripe_session")
public class StripeSession {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int stripeSessionId;
  private String sessionId;
  private int projectId;
  private int userId;
  private Date creationDate;
}

