package ai.quantumics.api.vo;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiIntegrationResponse {
  private String status;
  private List<Object> data;
  private String message;
}
