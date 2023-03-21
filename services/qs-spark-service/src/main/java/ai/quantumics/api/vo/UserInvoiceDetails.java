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
@JsonPropertyOrder({"userId","invoiceId","status","created","invoiceAmount","hostedInvoiceUrl","invoicePdf", "subscriptionId"})
public class UserInvoiceDetails {
  
  @JsonProperty private int userId;
  @JsonProperty private String invoiceId;
  @JsonProperty private String status;
  @JsonProperty private Date created;
  @JsonProperty private double invoiceAmount;
  @JsonProperty private String hostedInvoiceUrl;
  @JsonProperty private String invoicePdf;
  @JsonProperty private String subscriptionId;
  
}
