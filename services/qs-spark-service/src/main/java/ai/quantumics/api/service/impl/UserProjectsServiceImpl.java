package ai.quantumics.api.service.impl;



import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.QsUserProjects;
import ai.quantumics.api.repo.UserProjectsRepository;
import ai.quantumics.api.service.UserProjectsService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserProjectsServiceImpl implements UserProjectsService {
  
  @Autowired private UserProjectsRepository repository;

  @Override
  public List<QsUserProjects> getProjectUsersByUserId(int userId) throws SQLException {
    return repository.findByUserId(userId);
  }
  
  @Override
  public List<QsUserProjects> getProjectUsersByProjectId(int projectId) throws SQLException {
    return repository.findByProjectId(projectId);
  }

  @Override
  public QsUserProjects getUserProject(int userId, int projectId) {
    return repository.findByUserIdAndProjectId(userId, projectId);
  }

  @Override
  public QsUserProjects save(QsUserProjects userProject) throws SQLException {
    try {
      userProject = repository.save(userProject);
      log.info("Saved UserProject info successfully.");

      return userProject;
    } catch (Exception e) {
      log.info("Failed to save user information. Exception is: {}", e.getMessage());
      
      return null;
    }
  }

}
