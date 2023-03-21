package ai.quantumics.api.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSubscriptionInfo {
  
  private int userId;
  private String subscriptionType;
  private int validDays;
  private double explored;
  
}
