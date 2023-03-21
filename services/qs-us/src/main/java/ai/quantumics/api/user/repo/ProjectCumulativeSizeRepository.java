package ai.quantumics.api.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.user.model.ProjectCumulativeSizeInfo;

@Repository
public interface ProjectCumulativeSizeRepository extends JpaRepository<ProjectCumulativeSizeInfo, Integer> {
  
  ProjectCumulativeSizeInfo findByProjectIdAndUserId(int projectId, int userId);
  
}
