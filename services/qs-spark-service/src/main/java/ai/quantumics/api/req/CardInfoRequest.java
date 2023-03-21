package ai.quantumics.api.req;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CardInfoRequest {
  
  private String cardNumber;
  private String cardType;
  private String cardHolderName;
  private String expiryDate;
  private String cvv;
  private String address1;
  private String address2;
  private String city;
  private String country;
  private String zipcode;
  private int userId;
  
}
