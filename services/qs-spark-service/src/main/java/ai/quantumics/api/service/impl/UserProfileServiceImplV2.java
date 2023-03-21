package ai.quantumics.api.service.impl;



import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.QsUserProfileV2;
import ai.quantumics.api.repo.UserProfileRepositoryV2;
import ai.quantumics.api.service.UserProfileServiceV2;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserProfileServiceImplV2 implements UserProfileServiceV2 {
  
  @Autowired private UserProfileRepositoryV2 repository;

  @Override
  public QsUserProfileV2 getUserProfileById(int userId) throws SQLException {
    return repository.findByUserId(userId);
  }

  @Override
  public boolean save(QsUserProfileV2 userProfile) {
    try {
      repository.save(userProfile);
      
      log.info("Saved user profile info successfully.");
      
      return true;
    } catch (Exception e) {
      log.info("Failed to save user profile information. Exception is: {}", e.getMessage());
      
      return false;
    }
  }
}


