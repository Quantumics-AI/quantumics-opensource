package ai.quantumics.api.repo;

import org.springframework.data.repository.CrudRepository;
import ai.quantumics.api.model.SubscriptionInfo;

public interface SubscriptionInfoRepository extends CrudRepository<SubscriptionInfo, Integer> {

}
