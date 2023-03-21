package ai.quantumics.api.req;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetPasswordRequest {
  
  private String guid;
  private String emailId;
  private String newPassword;
  
}
