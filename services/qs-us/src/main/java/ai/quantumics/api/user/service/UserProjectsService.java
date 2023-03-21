package ai.quantumics.api.user.service;

import java.sql.SQLException;
import java.util.List;

import ai.quantumics.api.user.model.QsUserProjects;

public interface UserProjectsService {
  List<QsUserProjects> getProjectUsersByUserId(int userId) throws SQLException;
  
  List<QsUserProjects> getProjectUsersByProjectId(int projectId) throws SQLException;
  
  QsUserProjects getUserProject(int userId, int projectId);
  
  QsUserProjects save(final QsUserProjects userProject) throws SQLException;
}
