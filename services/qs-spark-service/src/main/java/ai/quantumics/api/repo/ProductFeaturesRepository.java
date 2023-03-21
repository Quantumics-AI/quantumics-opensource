package ai.quantumics.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.ProductFeatures;

@Repository
public interface ProductFeaturesRepository extends JpaRepository<ProductFeatures, Integer> {
  
}
