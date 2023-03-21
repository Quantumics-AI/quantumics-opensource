package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;
import ai.quantumics.api.model.EngFlowDatalineage;

public interface EngFlowDatalineageService {
  
  EngFlowDatalineage save(EngFlowDatalineage datalineage) throws SQLException;
  
  void saveAll(List<EngFlowDatalineage> datalineages) throws SQLException;
  
  EngFlowDatalineage getDataLineageById(int lineageId) throws SQLException;
  
  List<EngFlowDatalineage> getDataLineageByEngFlowId(int datalineageId) throws SQLException;

}
