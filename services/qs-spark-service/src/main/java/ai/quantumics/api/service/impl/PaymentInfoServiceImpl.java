package ai.quantumics.api.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.PaymentInfo;
import ai.quantumics.api.repo.PaymentInfoRepository;
import ai.quantumics.api.service.PaymentInfoService;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {
  
  PaymentInfoRepository paymentInfoRepo;
  
  public PaymentInfoServiceImpl(PaymentInfoRepository paymentInfoRepo) {
    this.paymentInfoRepo = paymentInfoRepo;
  }
  
  @Override
  public List<PaymentInfo> getPaymentHistoryOfUser(int userId) {
    return paymentInfoRepo.findByUserId(userId);
  }
  
}
