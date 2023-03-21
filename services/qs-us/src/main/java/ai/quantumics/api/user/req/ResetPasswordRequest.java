package ai.quantumics.api.user.req;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetPasswordRequest {
  
  private String guid;
  private String emailId;
  private String newPassword;
  
}
