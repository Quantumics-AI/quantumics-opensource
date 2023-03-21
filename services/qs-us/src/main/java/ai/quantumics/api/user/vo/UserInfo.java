package ai.quantumics.api.user.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"userId", "company", "companyRole", "email", "firstName", "lastName", "role", "phone", "country", "fullName", "userImage", "creationDate", "modifiedDate"})
public class UserInfo {
  @JsonProperty private int userId;
  @JsonProperty private String company;
  @JsonProperty private String companyRole;
  @JsonProperty private String email;
  @JsonProperty private String firstName;
  @JsonProperty private String lastName;
  @JsonProperty private String role;
  @JsonProperty private String phone;
  @JsonProperty private String country;
  @JsonProperty private String fullName;
  @JsonProperty private String userImage;
  @JsonProperty private Date creationDate;
  @JsonProperty private Date modifiedDate;
  
}
