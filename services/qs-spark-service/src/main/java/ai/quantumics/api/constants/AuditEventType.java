package ai.quantumics.api.constants;

public enum AuditEventType {

  PROJECT("Project"), 
  FOLDER("Folder"),
  INGEST("Ingest"),
  CLEANSE("Cleanse"),
  ENGINEER("Engineer"),
  GOVERN("Governance"),
  USER_PROFILE("User Profile"),
  SUB_USER("Sub User");
  
  
  private String eventType;
  
  AuditEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getEventType() {
    return eventType;
  }
  
}
