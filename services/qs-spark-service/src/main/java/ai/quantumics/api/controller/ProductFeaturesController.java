package ai.quantumics.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ai.quantumics.api.service.ProductFeaturesService;
import ai.quantumics.api.util.DbSessionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@RestController
@Api(value = "Quantumics Service API ")
public class ProductFeaturesController {

  private final ProductFeaturesService productFeaturesService;
  private final DbSessionUtil dbUtil;
  
  public ProductFeaturesController(ProductFeaturesService productFeaturesService, DbSessionUtil dbUtil) {
    this.productFeaturesService = productFeaturesService;
    this.dbUtil = dbUtil;
  }
  
  private ResponseEntity<Object> returnResInstance(HttpStatus code, String message, Object result){
    HashMap<String, Object> genericResponse = new HashMap<>();
    genericResponse.put("code", code.value());
    genericResponse.put("message", message);
    
    if(result != null) {
      genericResponse.put("result", result);
    }
    
    return ResponseEntity.status(code).body(genericResponse);
  }
  
  @ApiOperation(value = "product features", response = Json.class, notes = "Fetch supported product features.")
  @GetMapping(value = "/api/v1/productfeatures", produces = "application/json")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Product features fetched successfully."),
        @ApiResponse(code = 500, message = "Internal server error.")
      })
  public ResponseEntity<Object> getProductFeatures() {

    ResponseEntity<Object> response = null;
    try {
      
      log.info("Fetching product features...");
      dbUtil.changeSchema("public");
      
      Map<String, List<String>> productFeatures = productFeaturesService.getProductFeatures();
      if(productFeatures != null && !productFeatures.isEmpty()) {
        response = returnResInstance(HttpStatus.OK, "Product features fetched successfully.", productFeatures);
      } else {
        response = returnResInstance(HttpStatus.BAD_REQUEST, "Failed to fetch product features.", null);
      }
    } catch (final Exception e) {
      log.error("Error {} ", e.getMessage());
      response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
    }
    
    return response;
  }
  
}
