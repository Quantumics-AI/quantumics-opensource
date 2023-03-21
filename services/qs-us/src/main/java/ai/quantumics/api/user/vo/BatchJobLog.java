package ai.quantumics.api.user.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobLog {
  
  private int id;
  private int from;
  private int total;
  private List<String> log;
  
}
