package ai.quantumics.api.user.service.impl;

import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ai.quantumics.api.user.model.QsUserConsent;
import ai.quantumics.api.user.model.QsUserV2;
import ai.quantumics.api.user.repo.UserConsentRepository;
import ai.quantumics.api.user.repo.UserRepositoryV2;
import ai.quantumics.api.user.service.UserServiceV2;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImplV2 implements UserServiceV2 {
  @Autowired private UserRepositoryV2 repository;
  @Autowired private UserConsentRepository userconsentRepository;
  @Override
  public QsUserV2 getUserByEmail(String userEmail) throws SQLException {
    return repository.findByUserEmailAndActive(userEmail, true);
  }

  @Override
  public QsUserV2 getUserById(int userId) throws SQLException {
    return repository.findByUserId(userId);
  }
  
  @Override
  public QsUserV2 getActiveUserById(int userId) throws SQLException {
    return repository.findByUserIdAndActive(userId, true);
  }
  
  @Override
  public QsUserV2 getActiveUserByStripeCustomerId(String stripeCustomerId) throws SQLException {
    return repository.findByStripeCustomerIdAndActive(stripeCustomerId, true);
  }
  
  @Override
  public List<QsUserV2> getSubUsersOfParent(int userParentId) throws SQLException {
    return repository.findAllByUserParentIdAndActive(userParentId, true);
  }

  @Override
  public QsUserV2 save(QsUserV2 user) throws SQLException{
    /*
     * try { user = repository.save(user);
     * 
     * log.info("Saved user info successfully.");
     * 
     * return user; } catch (Exception e) {
     * log.info("Failed to save user information. Exception is: {}", e.getMessage());
     * 
     * return null; }
     */
    return repository.save(user);
  }
  
  @Override
  public boolean isExists(String userEmail) {
    return repository.existsByUserEmailAndActiveTrue(userEmail);
  }

  @Override
  public QsUserConsent saveUserConsent(QsUserConsent qsUserConsent) {
    return userconsentRepository.save(qsUserConsent);
  }

  @Override
  public Page<QsUserConsent> getUserConsentHistory(int userId, Pageable pageable) {
    return userconsentRepository.findAllByUserId(userId, pageable);
  }

}
