package ai.quantumics.api.service;

import java.sql.SQLException;
import ai.quantumics.api.model.ProjectCumulativeSizeInfo;

public interface ProjectCumulativeSizeService {
  
  ProjectCumulativeSizeInfo save(ProjectCumulativeSizeInfo info) throws SQLException;
  
  ProjectCumulativeSizeInfo getSizeInfoForProject(int projectId, int userId);
  
}
