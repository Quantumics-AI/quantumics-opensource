package ai.quantumics.api.req;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiIntegrationRequest {
  private int projectId;
  private int folderId;
  private String fileName;
  private String apiEndPoint;
  private String authType;
  private String bearerToken;
  private String userName;
  private String password; // Have to declare as char[] instead of String, security aspect.
  
  private String entity;// must be one of companies, contacts and so on
  private String apiKey;
}
