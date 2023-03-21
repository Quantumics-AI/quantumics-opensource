package ai.quantumics.api.service.impl;

import org.springframework.stereotype.Service;
import ai.quantumics.api.repo.UserSubscriptionRepository;
import ai.quantumics.api.service.UserSubscriptionService;

@Service
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

  UserSubscriptionRepository userSubscriptionRepo;
  
  public UserSubscriptionServiceImpl(UserSubscriptionRepository userSubscriptionRepo) {
    this.userSubscriptionRepo = userSubscriptionRepo;
  }
  
}
