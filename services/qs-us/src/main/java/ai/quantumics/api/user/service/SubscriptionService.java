package ai.quantumics.api.user.service;

import java.sql.SQLException;
import java.util.List;

import ai.quantumics.api.user.model.ProjectSubscriptionDetails;
import ai.quantumics.api.user.model.QsSubscription;

public interface SubscriptionService {
  
  QsSubscription getSubById(final int subId) throws SQLException;
	
  QsSubscription getSubscriptionById(final int subscriptionId) throws SQLException;

  QsSubscription getSubscriptionByNameAndPlanType(final String name, final String planType) throws SQLException;
  
  QsSubscription getSubscriptionByName(final String name) throws SQLException;

  List<QsSubscription> getSubscriptionListByName(final String name) throws SQLException;
  
  QsSubscription getSubscriptionByPlanTypeId(final String planTypeId) throws SQLException;
  
  List<QsSubscription> getAllSubscriptions() throws SQLException;
  
  ProjectSubscriptionDetails saveProjectSubscriptionDetails(ProjectSubscriptionDetails projectSubscriptionDetails) throws SQLException;
  
  List<ProjectSubscriptionDetails> getProjectSubscriptionDetails(int projectId, int userId) throws SQLException;
  
  List<ProjectSubscriptionDetails> getProjectSubscriptionDetailsByUser(int userId) throws SQLException;
  
  ProjectSubscriptionDetails getProjectSubscriptionDetails(int userId, String subscriptionType, String status) throws SQLException;
  
  ProjectSubscriptionDetails getProjectSubscriptionByStatus(int projectId, int userId, String status)
      throws SQLException;

  Iterable<ProjectSubscriptionDetails> saveAllProjectSubscriptionDetails(
      List<ProjectSubscriptionDetails> projectSubscriptionDetailsList) throws SQLException;

  ProjectSubscriptionDetails getProjectSubscriptionDetails(int projectId) throws SQLException;
  
}
