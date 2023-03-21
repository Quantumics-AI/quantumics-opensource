package ai.quantumics.api.repo;

import org.springframework.data.repository.CrudRepository;
import ai.quantumics.api.model.UserSubscription;

public interface UserSubscriptionRepository extends CrudRepository<UserSubscription, Integer> {

}
