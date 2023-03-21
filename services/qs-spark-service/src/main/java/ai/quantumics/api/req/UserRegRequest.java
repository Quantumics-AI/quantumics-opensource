package ai.quantumics.api.req;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRegRequest {
  private String userEmail;
  private String userPwd;
  private String userRole;
  private String userFirstName;
  private String userLastName;
  private String userCompany;
  private String companyRole;
  private String userPhone;
  private String userCountry;
  private String createdBy;
  private String subscriptionName;
  private String subscriptionPlanType;
  
  private int projectId;
  private int parentUserId;
}
