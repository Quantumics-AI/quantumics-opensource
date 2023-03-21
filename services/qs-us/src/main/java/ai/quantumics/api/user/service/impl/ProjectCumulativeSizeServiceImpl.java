package ai.quantumics.api.user.service.impl;

import java.sql.SQLException;
import org.springframework.stereotype.Service;
import ai.quantumics.api.user.model.ProjectCumulativeSizeInfo;
import ai.quantumics.api.user.repo.ProjectCumulativeSizeRepository;
import ai.quantumics.api.user.service.ProjectCumulativeSizeService;

@Service
public class ProjectCumulativeSizeServiceImpl implements ProjectCumulativeSizeService {
  
  private final ProjectCumulativeSizeRepository repository;
  
  public ProjectCumulativeSizeServiceImpl(ProjectCumulativeSizeRepository repository) {
    this.repository = repository;
  }
  
  @Override
  public ProjectCumulativeSizeInfo getSizeInfoForProject(int projectId, int userId) {
    return repository.findByProjectIdAndUserId(projectId, userId);
  }
  
  @Override
  public ProjectCumulativeSizeInfo save(ProjectCumulativeSizeInfo info) throws SQLException {
    return repository.save(info);
  }
}
