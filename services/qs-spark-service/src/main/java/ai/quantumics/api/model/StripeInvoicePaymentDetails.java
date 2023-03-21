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
@Table(name = "qs_invoice_payment_details")
public class StripeInvoicePaymentDetails {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int invoicePaymentId;
  private String invoiceId;
  private String accountCountry;
  private String accountName;
  private String accountTaxIds;
  private double amountDue;
  private double amountPaid;
  private double amountRemaining;  
  private String billingReason;  
  private String charge;
  private Date created;
  private String currency;
  private String customer;  
  private String customerAddress;
  private String customerName;
  private String customerPhone;
  private String customerShipping; 
  private String customerEmail;
  private String discount;
  private String hostedInvoiceUrl;  
  private String invoicePdf;
  private String lines;
  private boolean livemode;  
  private String status;
  private Date periodStart;  
  private Date periodEnd;
  private String subscription;  
  private double subtotal;
  private double tax;
  private double total;
  private String totalDiscountAmounts;
  private String stripeResponse;
  private Date creationDate;

}
