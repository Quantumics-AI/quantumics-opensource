package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "qs_payment_details")
public class StripePaymentDetails {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int paymentDetailsId;
  
  private String chargeId;
  private double amount;
  private double amountCaptured;
  private double amountRefunded;
  private String billingDetails;
  private boolean captured;
  private Date created;
  private String currency;
  private String customer;
  private String description;
  private String dispute;
  private boolean disputed;
  private String failureCode;
  private String failureMessage;
  private String invoice;
  private String outcome;
  private boolean paid;
  private String paymentIntent;
  private String paymentMethod;
  private String paymentMethodDetails;
  private boolean refunded;
  private String refunds;
  private String status;
  private String stripeResponse;
  private Date creationDate;
  
}
