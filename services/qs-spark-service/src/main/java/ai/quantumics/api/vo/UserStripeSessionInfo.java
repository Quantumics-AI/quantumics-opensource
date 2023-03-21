package ai.quantumics.api.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"userId", "tierName", "paymentStatus", "quantity", "created", "amountTax", "amountSubTotal", "amountTotal", "invoicePdf"})
public class UserStripeSessionInfo {
  
  private int userId;
  private String tierName;
  private String paymentStatus;
  private int quantity;
  private Date created;
  private double amountTax;
  private double amountSubTotal;
  private double amountTotal;
  private String invoicePdf;
  
}
