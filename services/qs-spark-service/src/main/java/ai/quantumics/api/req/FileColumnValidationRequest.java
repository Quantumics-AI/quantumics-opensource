package ai.quantumics.api.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileColumnValidationRequest {
  private int projectId;
  private int userId;
  private int folderId;
  private int fileId;
  private String columnName;
  private String targetDataType;
}
