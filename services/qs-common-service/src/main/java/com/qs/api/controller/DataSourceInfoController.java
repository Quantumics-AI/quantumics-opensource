package com.qs.api.controller;

import java.util.HashMap;
import java.util.List;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.qs.api.model.DataSourceInfo;
import com.qs.api.model.Projects;
import com.qs.api.service.DataSourceInfoService;
import com.qs.api.service.ProjectService;
import com.qs.api.util.DbSessionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@Api(value = "Quantumics Service API ")
@RestController
@Component
public class DataSourceInfoController {
  private final ProjectService projectService;
  private final DataSourceInfoService dataSourceInfoService;
  private final DbSessionUtil dbUtil;
  
  public DataSourceInfoController(ProjectService projectService,
      DataSourceInfoService datasourceTypeService, DbSessionUtil dbUtil) {
    this.projectService = projectService;
    this.dataSourceInfoService = datasourceTypeService;
    this.dbUtil = dbUtil;
  }
  
  private ResponseEntity<Object> returnResInstance(HttpStatus code, String message, Object result) {
    HashMap<String, Object> genericResponse = new HashMap<>();
    genericResponse.put("code", code.value());
    genericResponse.put("message", message);

    if (result != null) {
      genericResponse.put("result", result);
    }

    return ResponseEntity.status(code).body(genericResponse);
  }
  
  @GetMapping("/api/v1/getLogs")
	public String getlogs() {
		log.debug("Debug logs enabled!");
		log.info("Info logs enabled!");
		log.error("Error logs enabled!");
		log.trace("Trace logs enabled");
		log.warn("Warning logs enabled!");
		return "Success!";
	}
  
  @ApiOperation(value = "Save Datasource Type", response = Json.class)
  @PostMapping("/api/v1/datasourcesinfo")
  @ApiResponses(
      value = {@ApiResponse(code = 200, message = "Data source type saved successfully."),
          @ApiResponse(code = 400, message = "Failed to save the datasource type."),
          @ApiResponse(code = 500, message = "Internal error occured.")})
  public ResponseEntity<Object> saveDataSourceType(@RequestBody DataSourceInfo dataSourceInfo) {
    ResponseEntity<Object> response = null;
    try {
      dbUtil.changeSchema("public");
      Projects project = projectService.getProject(dataSourceInfo.getProjectId());
      dbUtil.changeSchema(project.getDbSchemaName());
      
      dataSourceInfo.setCreationDate(DateTime.now().toDate());
      
      DataSourceInfo dataSourceInfoSaved = dataSourceInfoService.saveFileInfo(dataSourceInfo); 
      if (dataSourceInfoSaved != null) {
        response = returnResInstance(HttpStatus.OK, "Data source type saved successfully.",
            dataSourceInfoSaved);
      } else {
        response = returnResInstance(HttpStatus.BAD_REQUEST,
            "Failed to save the datasource type.", null);
      }

    } catch (final Exception e) {
      log.error("Error {} ", e.getMessage());
      response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
    }

    return response;
  }
  
  @ApiOperation(value = "Fetch Datasource Types", response = Json.class)
  @ApiResponses(
      value = {@ApiResponse(code = 200, message = "Data source types returned successfully."),
          @ApiResponse(code = 400, message = "Failed to fetch the datasource types."),
          @ApiResponse(code = 500, message = "Internal error occured.")})
  @GetMapping("/api/v1/datasourcesinfo/{projectId}/{dataSourceType}")
  public ResponseEntity<Object> getDataSourceTypes(@PathVariable(value = "projectId") final int projectId, 
      @PathVariable(value = "dataSourceType") final String dataSourceType) {
    ResponseEntity<Object> response = null;
    try {
      dbUtil.changeSchema("public");
      Projects project = projectService.getProject(projectId);
      dbUtil.changeSchema(project.getDbSchemaName());
      
      List<DataSourceInfo> dsTypes = dataSourceInfoService.getDataSourcesByType(projectId, dataSourceType);
      if (dsTypes != null && !dsTypes.isEmpty()) {
        response = returnResInstance(HttpStatus.OK, "Data source types returned successfully.",
            dsTypes);
      } else {
        response = returnResInstance(HttpStatus.BAD_REQUEST,
            "Failed to fetch the datasource types.", null);
      }

    } catch (final Exception e) {
      log.error("Error {} ", e.getMessage());
      response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
    }

    return response;
  }
  
}
