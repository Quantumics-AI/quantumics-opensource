package ai.quantumics.api.vo;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessGlossaryResponse {
  private int id;
  private int projectId;
  private String term;
  private String definition;
  private String dataOwner;
  private String businessRules;
  private String acronym;
  private String synonymTerm;
  private String date;
  private String relatedAttributes;
  private String snAttributes;
  private String addTags;
  private boolean published;
  private Date creationDate;
  private Date modifiedDate;
  private int userId;
}
