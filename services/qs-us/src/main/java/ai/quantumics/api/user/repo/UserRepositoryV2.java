package ai.quantumics.api.user.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.user.model.QsUserV2;

@Repository
public interface UserRepositoryV2 extends JpaRepository<QsUserV2, Integer>{
  
  QsUserV2 findByUserId(int userId);
  
  QsUserV2 findByUserEmail(String userEmail);
  
  QsUserV2 findByUserEmailAndActive(String userEmail, boolean active);
  
  QsUserV2 findByUserIdAndActive(int userId, boolean active);
  
  QsUserV2 findByStripeCustomerIdAndActive(String stripeCustomerId, boolean active);
  
  List<QsUserV2> findAllByUserParentIdAndActive(int userParentId, boolean active);
  
  boolean existsByUserEmailAndActiveTrue(String userEmail);
  
}
