package ai.quantumics.api.user.service;

import java.sql.SQLException;
import java.util.List;

import ai.quantumics.api.user.model.QsUserConsent;
import ai.quantumics.api.user.model.QsUserV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserServiceV2 {
  
  QsUserV2 getUserByEmail(final String userEmail) throws SQLException;

  QsUserV2 getUserById(final int userId) throws SQLException;
  
  QsUserV2 getActiveUserById(final int userId) throws SQLException;
  
  QsUserV2 getActiveUserByStripeCustomerId(final String stripeCustomerId) throws SQLException;
  
  List<QsUserV2> getSubUsersOfParent(int userId) throws SQLException;

  QsUserV2 save(final QsUserV2 user) throws SQLException;
  
  boolean isExists(String userEmail);
  
  QsUserConsent saveUserConsent(final QsUserConsent qsUserConsent);

  Page<QsUserConsent> getUserConsentHistory(final int userId, Pageable pageable);
}
