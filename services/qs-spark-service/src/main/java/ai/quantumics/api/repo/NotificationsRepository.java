package ai.quantumics.api.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.QsNotifications;

@Repository
public interface NotificationsRepository extends JpaRepository<QsNotifications, Integer> {
  
  int countByUserIdInAndNotificationRead(List<Integer> userIds, boolean notificationRead);
  
  List<QsNotifications> findByUserIdIn(List<Integer> userIds);
  
  List<QsNotifications> findByUserIdInAndNotificationRead(List<Integer> userIds, boolean notificationRead);
  
}
