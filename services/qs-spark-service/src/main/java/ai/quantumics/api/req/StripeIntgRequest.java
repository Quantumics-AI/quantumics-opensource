package ai.quantumics.api.req;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StripeIntgRequest {
  
  private int userId;
  private String projectId;
  private String priceId;

}
