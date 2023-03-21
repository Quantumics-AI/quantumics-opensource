package ai.quantumics.api.repo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.EngFlowDatalineage;

@Repository
public interface EngFlowDatalineageRepository extends CrudRepository<EngFlowDatalineage, Integer> {
  
  EngFlowDatalineage findByLineageId(int lineageId);
  
  List<EngFlowDatalineage> findByEngFlowId(int engFlowId);
  
}
