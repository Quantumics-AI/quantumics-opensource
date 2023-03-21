/**
 * 
 */
package ai.quantumics.api.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class ChangePasswordRequest extends ResetPasswordRequest {
	private String oldPassword;
}
