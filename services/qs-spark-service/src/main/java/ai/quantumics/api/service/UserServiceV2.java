package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;
import ai.quantumics.api.model.QsUserConsent;
import ai.quantumics.api.model.QsUserV2;

public interface UserServiceV2 {
  
  QsUserV2 getUserByEmail(final String userEmail) throws SQLException;

  QsUserV2 getUserById(final int userId) throws SQLException;
  
  QsUserV2 getActiveUserById(final int userId) throws SQLException;
  
  QsUserV2 getActiveUserByStripeCustomerId(final String stripeCustomerId) throws SQLException;
  
  List<QsUserV2> getSubUsersOfParent(int userId) throws SQLException;

  QsUserV2 save(final QsUserV2 user) throws SQLException;
  
  boolean isExists(String userEmail);
  
  QsUserConsent saveUserConsent(final QsUserConsent qsUserConsent);
}
