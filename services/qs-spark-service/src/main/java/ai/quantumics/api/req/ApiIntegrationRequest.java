package ai.quantumics.api.req;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
public class ApiIntegrationRequest {
  private int projectId;
  private int userId;

  private int folderId;
  private Map<String, String> headers;
  private String apiEndPoint;
  private String httpMethodType;
  private AuthenticationType authType;
  private Authentication authentication;
  private String contextPath;

  public String getContextPath() {
    return contextPath;
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getProjectId() {
    return projectId;
  }

  public void setProjectId(int projectId) {
    this.projectId = projectId;
  }

  public int getFolderId() {
    return folderId;
  }

  public void setFolderId(int folderId) {
    this.folderId = folderId;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public String getApiEndPoint() {
    return apiEndPoint;
  }

  public void setApiEndPoint(String apiEndPoint) {
    this.apiEndPoint = apiEndPoint;
  }

  public String getHttpMethodType() {
    return httpMethodType;
  }

  public void setHttpMethodType(String httpMethodType) {
    this.httpMethodType = httpMethodType;
  }

  public AuthenticationType getAuthType() {
    return authType;
  }

  public void setAuthType(AuthenticationType authType) {
    this.authType = authType;
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }
}


