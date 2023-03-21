package ai.quantumics.api.constants;

public enum NotificationStatus {
  
  UNREAD("unread"), ALL("all"), COUNT("count");
  
  private String status;
  
  NotificationStatus(String status){
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
  
}
