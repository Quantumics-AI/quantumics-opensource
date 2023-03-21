package ai.quantumics.api.service;

import java.util.List;
import ai.quantumics.api.model.RedashViewInfo;

public interface RedashViewInfoService {
  
  List<RedashViewInfo> getViewsForSchema(String schemaName);
  
  RedashViewInfo getViewById(int redashViewId);
  
  RedashViewInfo getViewByName(String viewName);
  
  RedashViewInfo save(RedashViewInfo info);
  
}
