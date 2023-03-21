package ai.quantumics.api.repo;

import org.springframework.data.repository.CrudRepository;
import ai.quantumics.api.model.StripeSession;

public interface StripeSessionRepository extends CrudRepository<StripeSession, Integer> {

  StripeSession findBySessionId(String stripeSessionId);

}
