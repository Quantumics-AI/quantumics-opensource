package ai.quantumics.api.user.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "qs_email_templates")
public class EmailTemplate {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int templateId;
  
  private String name;
  private String action;
  
  @Column(columnDefinition = "TEXT")
  private String body;
  private Date creationDate;
}
