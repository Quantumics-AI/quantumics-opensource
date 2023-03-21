package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Qs_User_V2")
public class QsUserV2 {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int userId;

  private String userEmail;
  private String userPwd;
  private String salt;
  private String userRole;
  private String userRedashKey;
  private String userSubscriptionType;
  
  private int userParentId;
  private int userSubscriptionTypeId;
  private int userDefaultProjects;
  private int failedLoginAttemptsCount;
  
  private Date userSubscriptionValidFrom;
  private Date userSubscriptionValidTo;
  private Date modifiedDate;
  
  private boolean active;
  private boolean firstTimeLogin;
  private String stripeCustomerId;
  private String userSubscriptionPlanType;
  private String userType;
  
  
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", unique = true)
  private QsUserProfileV2 qsUserProfile;
  
  /*
   * @ManyToOne(fetch = FetchType.LAZY, optional = true)
   * 
   * @JoinColumn(name = "user_id", referencedColumnName = "user_id") private QsUserV2 qsUserV2;
   */

}
