package ai.quantumics.api.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.RedashViewInfo;

@Repository
public interface RedashViewInfoRepository extends JpaRepository<RedashViewInfo, Integer> {
  List<RedashViewInfo> findBySchemaName(String schemaName);
  
  RedashViewInfo findByRedashViewId(int redashViewId);
  
  RedashViewInfo findByViewName(String viewName);
  
}
