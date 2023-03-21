package ai.quantumics.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.ProjectCumulativeSizeInfo;

@Repository
public interface ProjectCumulativeSizeRepository extends JpaRepository<ProjectCumulativeSizeInfo, Integer> {
  
  ProjectCumulativeSizeInfo findByProjectIdAndUserId(int projectId, int userId);
  
}
