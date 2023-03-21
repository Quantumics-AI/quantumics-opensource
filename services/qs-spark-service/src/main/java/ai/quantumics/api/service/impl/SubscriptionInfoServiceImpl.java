package ai.quantumics.api.service.impl;

import org.springframework.stereotype.Service;
import ai.quantumics.api.repo.SubscriptionInfoRepository;
import ai.quantumics.api.service.SubscriptionInfoService;

@Service
public class SubscriptionInfoServiceImpl implements SubscriptionInfoService {
  SubscriptionInfoRepository subscriptionInfoRepo;
  
  public SubscriptionInfoServiceImpl(SubscriptionInfoRepository subscriptionInfoRepo) {
    this.subscriptionInfoRepo = subscriptionInfoRepo;
  }
  
}
