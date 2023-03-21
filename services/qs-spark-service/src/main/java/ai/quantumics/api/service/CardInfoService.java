package ai.quantumics.api.service;

import java.util.List;
import ai.quantumics.api.model.CardInfo;

public interface CardInfoService {
  
  public List<CardInfo> getCardsInfoOfUser(int userId);
  
  public CardInfo getCardInfoOfUserByFingerPrint(int userId, String fingerPrint);
  
  public boolean saveCardInfo(CardInfo cardInfo);
  
}
