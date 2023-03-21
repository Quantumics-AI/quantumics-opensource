package ai.quantumics.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.StripePaymentDetails;

@Repository
public interface StripePaymentDetailsRepository extends JpaRepository<StripePaymentDetails, Integer> {

}
