package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;
import ai.quantumics.api.model.QsNotifications;

public interface NotificationsService {
  
  List<QsNotifications> getAllNotifications(final List<Integer> userIds, final String type);
  
  int getNotificationsCountForUsers(final List<Integer> userIds);
  
  QsNotifications save(QsNotifications notification) throws SQLException;
  
}
