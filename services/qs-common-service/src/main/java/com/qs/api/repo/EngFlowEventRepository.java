package com.qs.api.repo;

import com.qs.api.model.EngFlowEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
public interface EngFlowEventRepository extends CrudRepository<EngFlowEvent, Integer> {

  List<EngFlowEvent> findByEngFlowId(int engFlowId);

  List<EngFlowEvent> findByEngFlowIdAndEventType(int engFlowId, String eventType);

  List<EngFlowEvent> findByEngFlowIdAndProjectIdAndEventTypeIn(int engFlowId, int projectId,
      Collection<String> eventTypes);
}
