package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "qs_card_info")
public class CardInfo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int cardInfoId;
  
  private String cardNumber;
  private String cardType;
  private String fingerPrint;
  private String expiryDate;
  private String address;
  private String emailId;
  private String name;
  private String country;
  private int userId;
  private boolean active;
  
  private Date creationDate;
  private Date modifiedDate;
  
}
