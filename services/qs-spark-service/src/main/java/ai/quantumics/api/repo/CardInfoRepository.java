package ai.quantumics.api.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.CardInfo;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Integer> {
  
  List<CardInfo> findByUserId(int userId);
  
  CardInfo findByUserIdAndFingerPrint(int userId, String fingerPrint);
  
}
