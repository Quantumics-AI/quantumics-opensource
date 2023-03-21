package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.ProjectCumulativeSizeInfo;
import ai.quantumics.api.repo.ProjectCumulativeSizeRepository;
import ai.quantumics.api.service.ProjectCumulativeSizeService;

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
