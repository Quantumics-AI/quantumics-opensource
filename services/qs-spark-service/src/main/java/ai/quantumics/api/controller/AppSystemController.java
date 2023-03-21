package ai.quantumics.api.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ai.quantumics.api.model.AppIncidents;
import ai.quantumics.api.model.AppSystemStatus;
import ai.quantumics.api.model.QsUserV2;
import ai.quantumics.api.service.UserServiceV2;
import ai.quantumics.api.service.impl.AppStatusServiceImpl;
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
public class AppSystemController {
  
  private final DbSessionUtil dbUtil;
  private final UserServiceV2 userService;
  private final AppStatusServiceImpl appStatusServiceImpl;
  
  public AppSystemController(DbSessionUtil dbUtil, UserServiceV2 userService, AppStatusServiceImpl appStatusServiceImpl) {
    this.dbUtil = dbUtil;
    this.userService = userService;
    this.appStatusServiceImpl = appStatusServiceImpl;
  }
  
  @ApiOperation(value = "Fetch the application status", response = Json.class, notes = "Application status information")
  @GetMapping("/api/v1/appsysteminfo/{userId}")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Fetched application status info successfully"),
      @ApiResponse(code = 400, message = "Failed to fetch the Application system status infomation.")})
  public ResponseEntity<Object>  getAppSystemStatusInfo(@PathVariable(name = "userId") int userId) {
    final HashMap<String, Object> genericResponse = new HashMap<>();
    try {
      dbUtil.changeSchema("public");
      
      QsUserV2 user = userService.getActiveUserById(userId);
      if(user == null) {
        genericResponse.put("code", HttpStatus.BAD_REQUEST.value());
        genericResponse.put("message", "Failed to fetch the Application system status infomation.");
      } else {
        
        List<AppIncidents> incidents = appStatusServiceImpl.getAppIncidentsInfo();
        List<AppSystemStatus> systemStatus = appStatusServiceImpl.getAppSystemStatusInfo();
        
        Map<String, Object> completeSysInfo = new HashMap<>();
        completeSysInfo.put("incidents", incidents);
        completeSysInfo.put("system_status", systemStatus);
        
        log.info("Completed fetching the Application system status information from the database...");
         
        genericResponse.put("code", HttpStatus.OK.value());
        genericResponse.put("message", "Fetched application status info successfully");
        genericResponse.put("result", completeSysInfo);
      }
    }catch(final SQLException exception) {
      genericResponse.put("code", HttpStatus.NOT_FOUND.value());
      genericResponse.put("message", exception.getMessage());
    }
    
    return ResponseEntity.status(HttpStatus.valueOf((Integer)genericResponse.get("code"))).body(genericResponse);
  }
  
}
