package ai.quantumics.api.user.service;

import java.sql.SQLException;

import ai.quantumics.api.user.model.ProjectCumulativeSizeInfo;

public interface ProjectCumulativeSizeService {
  
  ProjectCumulativeSizeInfo save(ProjectCumulativeSizeInfo info) throws SQLException;
  
  ProjectCumulativeSizeInfo getSizeInfoForProject(int projectId, int userId);
  
}
