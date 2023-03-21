package ai.quantumics.api.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ai.quantumics.api.model.PaymentInfo;
import ai.quantumics.api.model.QsUserV2;
import ai.quantumics.api.service.PaymentInfoService;
import ai.quantumics.api.service.UserServiceV2;
import ai.quantumics.api.util.DbSessionUtil;
import ai.quantumics.api.vo.UserPaymentInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@RestController
@Api(value = "Quantumics Service API ")
public class PaymentInfoController {

  private final DbSessionUtil dbUtil;
  private final UserServiceV2 userService;
  private final PaymentInfoService paymentInfoService;
  
  public PaymentInfoController(DbSessionUtil dbUtil,
        UserServiceV2 userService,
        PaymentInfoService paymentInfoService) {
    this.dbUtil = dbUtil;
    this.userService = userService;
    this.paymentInfoService = paymentInfoService;
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
  
  @ApiOperation(value = "userPaymentInfo", response = Json.class, notes = "Fetch payment information of user")
  @GetMapping(value = "/api/v1/paymentinfo/{userId}", produces = "application/json")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "User payment details returned successfully..!"),
        @ApiResponse(code = 400, message = "User not found!")
      })
  public ResponseEntity<Object> getPaymentInfoOfUser(
      @PathVariable("userId") final int userId) {
    
    ResponseEntity<Object> response = null;
    try {
      
      log.info("Get Payment information of User with Id: {}", userId);
      dbUtil.changeSchema("public");
      
      final QsUserV2 userObj = userService.getUserById(userId);
      
      if(userObj != null) {
        List<PaymentInfo> payments = paymentInfoService.getPaymentHistoryOfUser(userId);
        
        if(payments != null && !payments.isEmpty()) {
          List<UserPaymentInfo> userPayments = new ArrayList<>();
          
          payments.forEach((payment) -> {userPayments.add(new UserPaymentInfo(payment.getUserId(),
                                                                              payment.getInvoiceId(),
                                                                              payment.getPaymentStatus(),
                                                                              payment.getCurrencyCode(),
                                                                              payment.getTransactionDate(),
                                                                              payment.getTransactionAmount()));
                                        });
          
          response = returnResInstance(HttpStatus.OK, "Fetched the payment history of the user with Id: "+ userId, userPayments);
          log.info("Retrieved the payment history details of the user: {}", userId);
        }else {
          response = returnResInstance(HttpStatus.OK, "No payment history found for the User with Id: "+ userId, Collections.emptyList());
          log.info("No payment history found for the user: {}", userId);
        }

      }else {
        response = returnResInstance(HttpStatus.BAD_REQUEST, "User not found.", null);
        log.info("User not found with Id: {}", userId);
      }
    }catch(Exception e) {
      log.error("Error {} ", e.getMessage());
      response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
    }
    return response;
  }
}
