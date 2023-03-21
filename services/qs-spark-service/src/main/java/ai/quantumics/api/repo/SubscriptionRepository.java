package ai.quantumics.api.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.QsSubscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<QsSubscription, Integer> {
  
  QsSubscription findBySubId(int subId);
  
  QsSubscription findBySubscriptionId(int subscriptionId);
  
  QsSubscription findByNameAndPlanType(String name, String planType);
  
  QsSubscription findByName(String name);
  
  QsSubscription findByPlanTypeIdAndActiveTrue(String planTypeId);
  
  List<QsSubscription> findByActive(boolean active);
  
}
