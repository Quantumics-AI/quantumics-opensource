package ai.quantumics.api.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Qs_Subscription_v2")
public class QsSubscription {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int subId;
  
  private int subscriptionId;
  private String name;
  private String planType;
  private double planTypePrice;
  private int accounts;
  private double planTypeAccountPrice;
  private int validityDays;
  private String planTypeId;
  private String planTypeCurrency;
  private String planTypeSettings;
  private boolean active;
}
