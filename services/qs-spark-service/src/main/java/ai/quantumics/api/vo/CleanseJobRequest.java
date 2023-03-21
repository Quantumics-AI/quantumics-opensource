package ai.quantumics.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleanseJobRequest {
  
  private int projectId;
  private int fileId;
  private int runJobId;
  
}
