package ai.quantumics.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"userId", "cardNumber", "cardType", "cardHolderName", "expiryDate", "address"})
public class UserCardInfo {
  
  @JsonProperty private int userId;
  @JsonProperty private String cardNumber;
  @JsonProperty private String cardType;
  @JsonProperty private String cardHolderName;
  @JsonProperty private String expiryDate;
  @JsonProperty private String address;
  
}
