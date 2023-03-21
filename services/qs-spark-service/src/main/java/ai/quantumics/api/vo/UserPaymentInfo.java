package ai.quantumics.api.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"userId", "invoiceId", "paymentStatus", "currencyCode", "transactionDate", "transactionAmount"})
public class UserPaymentInfo {
  
  @JsonProperty private int userId;
  @JsonProperty private String invoiceId;
  @JsonProperty private String paymentStatus;
  @JsonProperty private String currencyCode; 
  @JsonProperty private Date transactionDate;
  @JsonProperty private double transactionAmount;
  
  
}
