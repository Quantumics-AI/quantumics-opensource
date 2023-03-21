package ai.quantumics.api.user.repo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import ai.quantumics.api.user.model.ProjectSubscriptionDetails;

public interface SubscriptionDetailsRepository extends CrudRepository<ProjectSubscriptionDetails, Integer> {

  ProjectSubscriptionDetails findByProjectIdAndUserIdAndActiveTrue(int projectId, int userId);
  
  ProjectSubscriptionDetails findByProjectIdAndUserIdAndSubscriptionStatusAndActiveTrue(int projectId, int userId, String status);

  List<ProjectSubscriptionDetails> findByProjectIdAndUserIdOrderByCreationDateDesc(int projectId, int userId);
  
  List<ProjectSubscriptionDetails> findByUserIdOrderByCreationDateDesc(int userId);
  
  ProjectSubscriptionDetails findByUserIdAndSubscriptionTypeAndSubscriptionStatusAndActiveTrue(int userId, String subscriptionType, String status);
  
  ProjectSubscriptionDetails findByProjectIdAndActiveTrue(int projectId);
  
}
