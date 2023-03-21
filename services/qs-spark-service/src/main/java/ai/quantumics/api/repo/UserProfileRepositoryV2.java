package ai.quantumics.api.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.QsUserProfileV2;

@Repository
public interface UserProfileRepositoryV2 extends JpaRepository<QsUserProfileV2, Integer>{
  
  QsUserProfileV2 findByUserId(int userId);

}
