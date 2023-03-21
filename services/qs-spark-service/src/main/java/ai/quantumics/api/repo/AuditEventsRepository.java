package ai.quantumics.api.repo;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.AuditEvents;


@Repository
public interface AuditEventsRepository extends JpaRepository<AuditEvents, Integer>{
  
  List<AuditEvents> findByProjectIdAndActiveTrueOrderByAuditIdDesc(int projectId);

  List<AuditEvents> findByProjectIdAndActiveTrueAndCreationDateBetweenOrderByAuditIdDesc(int projectId, Date startDate, Date endDate);
  
  List<AuditEvents> findByProjectIdAndActiveTrueAndUserIdAndIsNotifyTrueAndIsNotifyReadOrderByAuditIdDesc(int projectId, int userId, boolean read);
  
  List<AuditEvents> findByProjectIdAndActiveTrueAndUserIdAndIsNotifyTrueOrderByAuditIdDesc(int projectId, int userId);

  Integer countByProjectIdAndActiveTrueAndUserIdAndIsNotifyTrueAndIsNotifyReadFalse(int projectId, int userId);
}
