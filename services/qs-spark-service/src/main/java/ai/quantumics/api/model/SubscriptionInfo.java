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
@Table(name = "qs_subscription_info")
public class SubscriptionInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int subscriptionId;
  
  private String subscriptionType;
  private String subscriptionKey;
  private int validityPeriod;
  private String planType;
  private String subscriptionDetails;
  private boolean active;
  private Date creationDate;
  private Date modifiedDate;
  
}
