package ai.quantumics.api.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.RedashViewInfo;
import ai.quantumics.api.repo.RedashViewInfoRepository;
import ai.quantumics.api.service.RedashViewInfoService;

@Service
public class RedashViewInfoServiceImpl implements RedashViewInfoService {
  
  private final RedashViewInfoRepository redashViewInfoRepo;
  
  public RedashViewInfoServiceImpl(RedashViewInfoRepository redashViewInfoRepo) {
    this.redashViewInfoRepo = redashViewInfoRepo;
  }

  @Override
  public List<RedashViewInfo> getViewsForSchema(String schemaName) {
    return redashViewInfoRepo.findBySchemaName(schemaName);
  }

  @Override
  public RedashViewInfo getViewById(int redashViewId) {
    return redashViewInfoRepo.findByRedashViewId(redashViewId);
  }

  @Override
  public RedashViewInfo getViewByName(String viewName) {
    return redashViewInfoRepo.findByViewName(viewName);
  }
  
  @Override
  public RedashViewInfo save(RedashViewInfo info) {
    return redashViewInfoRepo.save(info);
  }

}
