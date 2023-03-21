package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.model.QsNotifications;
import ai.quantumics.api.repo.NotificationsRepository;
import ai.quantumics.api.service.NotificationsService;

@Service
public class NotificationsServiceImpl implements NotificationsService {
  
  private final NotificationsRepository notificationsRepo;
  
  public NotificationsServiceImpl(NotificationsRepository notificationsRepo) {
    this.notificationsRepo = notificationsRepo;
  }

  @Override
  public List<QsNotifications> getAllNotifications(final List<Integer> userIds, final String type) {
    
    if(QsConstants.ALL_NOTIFS.equals(type))
      return notificationsRepo.findByUserIdIn(userIds);
    else if(QsConstants.UNREAD_NOTIFS.equals(type))
      return notificationsRepo.findByUserIdInAndNotificationRead(userIds, false);
    else
      return Collections.emptyList();
    
  }
  
  @Override
  public int getNotificationsCountForUsers(List<Integer> userIds) {
    return notificationsRepo.countByUserIdInAndNotificationRead(userIds, false);
  }
  
  @Override
  public QsNotifications save(QsNotifications notification) throws SQLException {
    return notificationsRepo.save(notification);
  }

}
