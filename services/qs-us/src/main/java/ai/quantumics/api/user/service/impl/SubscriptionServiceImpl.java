package ai.quantumics.api.user.service.impl;

import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ai.quantumics.api.user.model.ProjectSubscriptionDetails;
import ai.quantumics.api.user.model.QsSubscription;
import ai.quantumics.api.user.repo.SubscriptionDetailsRepository;
import ai.quantumics.api.user.repo.SubscriptionRepository;
import ai.quantumics.api.user.service.SubscriptionService;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
  @Autowired private SubscriptionRepository subscriptionRepository;
  @Autowired private SubscriptionDetailsRepository subscriptionDetailsRepository;
  
  @Override
  public QsSubscription getSubById(int subId) throws SQLException {
    return subscriptionRepository.findBySubId(subId);
  }
  
  @Override
  public QsSubscription getSubscriptionById(int subscriptionId) throws SQLException {
    return subscriptionRepository.findBySubscriptionId(subscriptionId);
  }

  @Override
  public QsSubscription getSubscriptionByNameAndPlanType(String name, String planType) throws SQLException {
    return subscriptionRepository.findByNameAndPlanType(name, planType);
  }
  
  @Override
  public List<QsSubscription> getSubscriptionListByName(final String name) throws SQLException{
	  return subscriptionRepository.findListByName(name);
  }
  
  @Override
  public QsSubscription getSubscriptionByName(String name) throws SQLException {
    return subscriptionRepository.findByName(name);
  }
  
  @Override
  public QsSubscription getSubscriptionByPlanTypeId(String planTypeId) throws SQLException {
    return subscriptionRepository.findByPlanTypeIdAndActiveTrue(planTypeId);
  }
  
  @Override
  public List<QsSubscription> getAllSubscriptions() throws SQLException {
    return subscriptionRepository.findByActive(true);
  }

  @Override
  public ProjectSubscriptionDetails saveProjectSubscriptionDetails(
      ProjectSubscriptionDetails projectSubscriptionDetails) throws SQLException {
    return subscriptionDetailsRepository.save(projectSubscriptionDetails);
  }
  
  @Override
  public Iterable<ProjectSubscriptionDetails> saveAllProjectSubscriptionDetails(
      List<ProjectSubscriptionDetails> projectSubscriptionDetailsList) throws SQLException {
    return subscriptionDetailsRepository.saveAll(projectSubscriptionDetailsList);
  }

  @Override
  public List<ProjectSubscriptionDetails> getProjectSubscriptionDetails(int projectId, int userId)
      throws SQLException {
    return subscriptionDetailsRepository.findByProjectIdAndUserIdOrderByCreationDateDesc(projectId, userId);
  }
  
  @Override
  public List<ProjectSubscriptionDetails> getProjectSubscriptionDetailsByUser(int userId)
      throws SQLException {
    return subscriptionDetailsRepository.findByUserIdOrderByCreationDateDesc(userId);
  }

  
  @Override
  public ProjectSubscriptionDetails getProjectSubscriptionByStatus(int projectId, int userId, String status)
      throws SQLException {
    return subscriptionDetailsRepository.findByProjectIdAndUserIdAndSubscriptionStatusAndActiveTrue(projectId, userId, status);
  }
  
  @Override
  public ProjectSubscriptionDetails getProjectSubscriptionDetails(int userId, String subscriptionType, String status)
      throws SQLException {
    return subscriptionDetailsRepository.findByUserIdAndSubscriptionTypeAndSubscriptionStatusAndActiveTrue(userId, subscriptionType, status);
  }
  
  @Override
  public ProjectSubscriptionDetails getProjectSubscriptionDetails(int projectId)
      throws SQLException {
    return subscriptionDetailsRepository.findByProjectIdAndActiveTrue(projectId);
  }
  

}
