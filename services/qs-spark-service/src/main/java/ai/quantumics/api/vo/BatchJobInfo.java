package ai.quantumics.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobInfo {
  
  private int batchJobId;
  private String batchJobStatus;
  private String batchJobLog;
  private long batchJobDuration;
  
}
