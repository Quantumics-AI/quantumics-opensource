package com.qs.api.controller;

import java.util.ArrayList;
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

import com.qs.api.model.Projects;
import com.qs.api.model.QsUdf;
import com.qs.api.request.UdfInfo;
import com.qs.api.service.ProjectService;
import com.qs.api.service.UdfService;
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
public class UdfController {
  private final ProjectService projectService;
  private final DbSessionUtil dbUtil;
  private final UdfService udfService;

  public UdfController(ProjectService projectService, DbSessionUtil dbUtil, UdfService udfService) {
    this.projectService = projectService;
    this.dbUtil = dbUtil;
    this.udfService = udfService;
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

  @ApiOperation(value = "List User Defined functions for a Project", response = Json.class)
  @GetMapping("/api/v1/udf/{projectId}")
  @ApiResponses(
      value = {@ApiResponse(code = 200, message = "UDF's for a project returned successfully."),
          @ApiResponse(code = 400, message = "Failed to fetch the UDF's for a project."),
          @ApiResponse(code = 500, message = "Internal error occured.")})
  public ResponseEntity<Object> getUdfsForProject(
      @PathVariable(value = "projectId") final int projectId) {
    ResponseEntity<Object> response = null;
    try {
      dbUtil.changeSchema("public");
      final Projects project = projectService.getProject(projectId);
      dbUtil.changeSchema(project.getDbSchemaName());

      List<QsUdf> udfs = udfService.getUdfByProjectId(projectId);

      if (udfs != null && !udfs.isEmpty()) {

        List<UdfInfo> udfsInfo = new ArrayList<>();
        /*udfs.stream().forEach((udf) -> udfsInfo.add(new UdfInfo(udf.getUdfId(), udf.getProjectId(),
            udf.getUdfName(), udf.getUdfInfo(), udf.getArguments(), udf.isActive(),
            udf.getCreatedDate(), udf.getModifiedDate(), udf.getCreatedBy(), udf.getModifiedBy())));*/

        response = returnResInstance(HttpStatus.OK, "UDF's for a project returned successfully.",
            udfsInfo);
      } else {
        response = returnResInstance(HttpStatus.BAD_REQUEST,
            "Failed to fetch the UDF's for a project.", null);
      }

    } catch (final Exception e) {
      log.error("Error {} ", e.getMessage());
      response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
    }

    return response;
  }

  @ApiOperation(value = "Fetch User Defined Function by Id", response = Json.class)
  @GetMapping("/api/v1/udf/{projectId}/{udfId}")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "UDF details returned successfully."),
      @ApiResponse(code = 400, message = "Failed to fetch the UDF details."),
      @ApiResponse(code = 500, message = "Internal error occured.")})
  public ResponseEntity<Object> getUdf(@PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "udfId") final int udfId) {
    ResponseEntity<Object> response = null;
    try {
      dbUtil.changeSchema("public");
      final Projects project = projectService.getProject(projectId);
      dbUtil.changeSchema(project.getDbSchemaName());

      QsUdf udf = udfService.getByUdfIdAndProjectIdAndUserId(udfId, projectId, 0);

      if (udf != null) {
        /*UdfInfo udfInfo = new UdfInfo(udf.getUdfId(), udf.getProjectId(), udf.getUdfName(),
            udf.getUdfInfo(), udf.getArguments(), udf.isActive(), udf.getCreatedDate(),
            udf.getModifiedDate(), udf.getCreatedBy(), udf.getModifiedBy());*/

        response = returnResInstance(HttpStatus.OK, "UDF info returned successfully.", /*udfInfo*/ null);
      } else {
        response = returnResInstance(HttpStatus.BAD_REQUEST, "Failed to fetch the UDF info.", null);
      }

    } catch (final Exception e) {
      log.error("Error {} ", e.getMessage());
      response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
    }

    return response;
  }

  @PostMapping("/api/v1/udf")
  public ResponseEntity<Object> insertUdf(@RequestBody UdfInfo udfInfo) {
    ResponseEntity<Object> response = null;
    log.info("Received create User Defined Function request with data: {}", udfInfo);
    try {
      dbUtil.changeSchema("public");
      final Projects project = projectService.getProject(udfInfo.getProjectId());
      log.info("Switching to the Private schema: {}", project.getDbSchemaName());
      dbUtil.changeSchema(project.getDbSchemaName());

      QsUdf qsUdf = new QsUdf();
      /*qsUdf.setProjectId(udfInfo.getProjectId());
      qsUdf.setUdfName(udfInfo.getUdfName());
      qsUdf.setUdfInfo(udfInfo.getUdfInfo());
      qsUdf.setArguments(udfInfo.getArguments());

      qsUdf.setActive(true);
      qsUdf.setCreatedBy(udfInfo.getCreatedBy());
      qsUdf.setCreatedDate(DateTime.now().toDate());*/

      QsUdf qsUdfTmp = udfService.save(qsUdf);
      if (qsUdfTmp != null) {
        response = returnResInstance(HttpStatus.OK, "UDF created successfully.", qsUdfTmp);
      }

    } catch (Exception e) {
      log.error("Error {} ", e.getMessage());
      response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to create UDF because: " + e.getMessage(), null);
    }

    return response;
  }

}
