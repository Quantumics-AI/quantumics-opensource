package ai.quantumics.api.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class SubscriptionsResponse {
  
  private int subscriptionId;
  private String name;
  private String planType;
  private double planTypePrice;
  private double planTypeAccountPrice;
  private String planTypeId;
  
}
