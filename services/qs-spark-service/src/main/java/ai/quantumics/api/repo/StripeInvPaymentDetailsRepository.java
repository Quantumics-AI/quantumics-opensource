package ai.quantumics.api.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ai.quantumics.api.model.StripeInvoicePaymentDetails;

public interface StripeInvPaymentDetailsRepository extends JpaRepository<StripeInvoicePaymentDetails, Integer> {
  
  List<StripeInvoicePaymentDetails> findByCustomerOrderByCreatedDesc(String customer);

  StripeInvoicePaymentDetails findBySubscriptionOrderByCreatedDesc(String subscription);
  
  StripeInvoicePaymentDetails findByInvoiceIdOrderByCreatedDesc(String invoiceId);
  
}
