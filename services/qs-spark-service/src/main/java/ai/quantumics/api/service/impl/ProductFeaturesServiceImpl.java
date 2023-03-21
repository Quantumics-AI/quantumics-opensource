package ai.quantumics.api.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.ProductFeatures;
import ai.quantumics.api.repo.ProductFeaturesRepository;
import ai.quantumics.api.service.ProductFeaturesService;

@Service
public class ProductFeaturesServiceImpl implements ProductFeaturesService {
  
  private ProductFeaturesRepository productFeaturesRepo;
  
  public ProductFeaturesServiceImpl(ProductFeaturesRepository productFeaturesRepo) {
    this.productFeaturesRepo = productFeaturesRepo;
  }

  @Override
  public Map<String, List<String>> getProductFeatures() {
    List<ProductFeatures> productFeatures = productFeaturesRepo.findAll();
    Map<String, List<String>> featuresMap = new HashMap<>();
    
    if(productFeatures != null && !productFeatures.isEmpty()) {
      productFeatures.stream().forEach((feature) -> {
        List<String> featuresList = featuresMap.get(feature.getModule());
        if(featuresList == null) {
          featuresList = new ArrayList<String>();
          featuresList.add(feature.getFeature());
        }else {
          featuresList.add(feature.getFeature());
        }
        
        featuresMap.put(feature.getModule(), featuresList);
      });
    }
    
    return featuresMap;
  }

}
