package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "qs_product_features")
@Data
public class ProductFeatures {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  
  private String module;
  private String feature;
  private boolean active;
  private String subscriptionType;
  private Date creationDate;
  private Date modifiedDate;
  
}
