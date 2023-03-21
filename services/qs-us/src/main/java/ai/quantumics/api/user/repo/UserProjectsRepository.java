package ai.quantumics.api.user.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.user.model.QsUserProjects;

@Repository
public interface UserProjectsRepository extends JpaRepository<QsUserProjects, Integer>{
  
  List<QsUserProjects> findByUserId(int userId);
  
  List<QsUserProjects> findByProjectId(int projectId);  
  
  QsUserProjects findByUserIdAndProjectId(int userId, int projectId);
}
