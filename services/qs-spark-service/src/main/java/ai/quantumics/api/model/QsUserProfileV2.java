package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "qs_Userprofile")
public class QsUserProfileV2 {

  @Id
  private int userId;
  
  private String userFirstName;
  private String userLastName;
  private String userMiddleName;
  private String userCompany;
  private String userCompanyRole;
  private String userDepart;
  private String userPhone;
  private String userCountry;
  private Date creationDate;
  private String createdBy;
  private String modifiedBy;
  private Date modifiedDate;
  
  private String userImage;
  
  @OneToOne(mappedBy = "qsUserProfile")
  private QsUserV2 qsUser;

}
