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
@Table(name = "qs_payment_info")
public class PaymentInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int paymentId;
  
  private int userId;
  private String invoiceId;
  private String transactionId;
  private Date transactionDate;
  private double transactionAmount;
  private double tax;
  private String currencyCode;
  private String paymentStatus;
  private String country;
  private Date creationDate;
  private Date modifiedDate;
  
}
