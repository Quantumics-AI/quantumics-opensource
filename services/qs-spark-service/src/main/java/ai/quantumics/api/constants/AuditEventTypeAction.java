package ai.quantumics.api.constants;

public enum AuditEventTypeAction {

  CREATE("Create"),
  UPDATE("Update"),
  DELETE("Delete"),
  EXECUTED("Executed"),
  SUCCEEDED("Succeeded"),
  FAILED("Failed"),
  INITIATED("Initiated"),
  PUBLISHED("Published"),
  DOWNLOADED("Downloaded");
 
  
  private String action;
  
  AuditEventTypeAction(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  
}
