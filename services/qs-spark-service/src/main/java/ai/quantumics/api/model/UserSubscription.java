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
@Table(name = "qs_user_subscription")
public class UserSubscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int userSubscriptionId;
  
  private int userId;
  private int subscriptionId;
  private int paymentId;
  private int cardInfoId;
  private boolean active;
  private Date creationDate;
  private Date modifiedDate;
  
}
