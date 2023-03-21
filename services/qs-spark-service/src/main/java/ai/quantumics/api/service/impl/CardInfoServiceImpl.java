package ai.quantumics.api.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.CardInfo;
import ai.quantumics.api.repo.CardInfoRepository;
import ai.quantumics.api.service.CardInfoService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CardInfoServiceImpl implements CardInfoService {
  
  CardInfoRepository cardInfoRepo;

  public CardInfoServiceImpl(CardInfoRepository cardInfoRepo) {
    this.cardInfoRepo = cardInfoRepo;
  }
  
  @Override
  public List<CardInfo> getCardsInfoOfUser(int userId) {
    return cardInfoRepo.findByUserId(userId);
  }
  
  @Override
  public CardInfo getCardInfoOfUserByFingerPrint(int userId, String fingerPrint) {
    return cardInfoRepo.findByUserIdAndFingerPrint(userId, fingerPrint);
  }
  
  @Override
  public boolean saveCardInfo(CardInfo cardInfo) {
    try {
      CardInfo ci = cardInfoRepo.save(cardInfo);
      if(ci != null) {
        return true;
      }
    }catch(Exception e) {
      log.error("Failed to store the card information of the user: {}", e.getMessage());
    }
    
    return false;
  }
  
}
