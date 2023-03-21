package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.EngFlowDatalineage;
import ai.quantumics.api.repo.EngFlowDatalineageRepository;
import ai.quantumics.api.service.EngFlowDatalineageService;

@Service
public class EngFlowDatalineageServiceImpl implements EngFlowDatalineageService {
  
  private final EngFlowDatalineageRepository engFlowDatalineageRepo;
  
  public EngFlowDatalineageServiceImpl(EngFlowDatalineageRepository engFlowDatalineageRepo) {
    this.engFlowDatalineageRepo = engFlowDatalineageRepo;
  }

  @Override
  public EngFlowDatalineage save(EngFlowDatalineage datalineage) throws SQLException {
    return engFlowDatalineageRepo.save(datalineage);
  }
  
  @Override
  public void saveAll(List<EngFlowDatalineage> datalineages) throws SQLException {
    engFlowDatalineageRepo.saveAll(datalineages);
  }
  
  @Override
  public List<EngFlowDatalineage> getDataLineageByEngFlowId(int engFlowId) throws SQLException {
    return engFlowDatalineageRepo.findByEngFlowId(engFlowId);
  }
  
  @Override
  public EngFlowDatalineage getDataLineageById(int lineageId) throws SQLException {
    return engFlowDatalineageRepo.findByLineageId(lineageId);
  }

}
