package ai.quantumics.api.user.req;

import lombok.Data;

@Data
public class UploadFileRequest {
  private String csvFilePath;
  private String dropColumns;
  private String encryptPiiColumns;
  private int folderId;
}
