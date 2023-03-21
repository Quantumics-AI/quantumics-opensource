package ai.quantumics.api.service;

import java.util.List;
import ai.quantumics.api.model.PaymentInfo;

public interface PaymentInfoService {
  
  public List<PaymentInfo> getPaymentHistoryOfUser(int userId);
    
}
