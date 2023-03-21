/**
 * 
 */
package ai.quantumics.api.service;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import ai.quantumics.api.model.AuditEvents;
import ai.quantumics.api.vo.AuditEventResponse;
import ai.quantumics.api.vo.Notification;

public interface AuditEventsService {
  
  void saveAuditEvents(AuditEvents auditEvents) throws SQLException ;
  
  List<AuditEventResponse> getAuditEvents(Integer projectId, String subscriptionType, String startDate, String endDate) throws SQLException, ParseException;
  
  List<Notification> getAllNotifications(Integer projectId, Integer userId) throws SQLException ;
  
  List<Notification> getAllNewNotifications(Integer projectId, Integer userId) throws SQLException ;
  
  Integer getNotificationCounts(Integer projectId, Integer userId) throws SQLException ;

  void setNotificationAsRead(List<Notification> notificationList) throws SQLException ;

  Notification update(int notificationId) throws SQLException;

}
