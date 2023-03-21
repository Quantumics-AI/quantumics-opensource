package ai.quantumics.api.service;



import java.sql.SQLException;

import ai.quantumics.api.model.QsUserProfileV2;

public interface UserProfileServiceV2 {
  QsUserProfileV2 getUserProfileById(final int userId) throws SQLException;

  boolean save(final QsUserProfileV2 userProfile);
}
