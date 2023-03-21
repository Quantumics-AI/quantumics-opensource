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
@Table(name = "qs_project_subscription_details")
public class ProjectSubscriptionDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int projectSubscriptionId;
  private int projectId;
  private int userId;
  private String subscriptionType;
  private String subscriptionPlanType;
  private int subscriptionPlanTypeId;
  private Date subscriptionValidFrom;
  private Date subscriptionValidTo;
  private String subscriptionId;
  private String invoiceId;
  private String chargeStatus;
  private String subscriptionStatus;
  private boolean active;
  private Date creationDate;
  private Double subTotal;
  private Double total;
  private Double tax;
  
}
