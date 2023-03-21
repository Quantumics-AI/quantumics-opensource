package com.qs.api.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.qs.api.model.QsDatasourceTypes;
import com.qs.api.request.DatasourceTypesInfo;
import com.qs.api.service.DatasourceTypeService;
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
public class DatasourceController {

	private final DbSessionUtil dbUtil;
	private final DatasourceTypeService datasourceTypeService;
	private final ControllerHelper controllerHelper;

	public DatasourceController(DbSessionUtil dbUtil,
			DatasourceTypeService datasourceTypeService,
			ControllerHelper controllerHelper) {
		this.controllerHelper = controllerHelper;
		this.dbUtil = dbUtil;
		this.datasourceTypeService = datasourceTypeService;
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

	@ApiOperation(value = "List Datasource Types", response = Json.class)
	@GetMapping("/api/v1/datasourcetypes")
	@ApiResponses(
			value = {@ApiResponse(code = 200, message = "Data source types returned successfully."),
					@ApiResponse(code = 400, message = "Failed to fetch the datasource types."),
					@ApiResponse(code = 500, message = "Internal error occured.")})
	public ResponseEntity<Object> getDataSourceTypes() {
		ResponseEntity<Object> response = null;
		try {
			dbUtil.changeSchema("public");

			List<QsDatasourceTypes> dsTypes = datasourceTypeService.getAllActiveDsTypes();
			if (dsTypes != null && !dsTypes.isEmpty()) {

				List<DatasourceTypesInfo> dsTypesInfo = new ArrayList<>();
				dsTypes.stream()
				.forEach((dsType) -> dsTypesInfo.add(new DatasourceTypesInfo(dsType.getDataSourceId(),
						dsType.getDataSourceType(), dsType.getDataSourceName(), dsType.getDataSourceImage(),
						dsType.isActive(), dsType.getCreationDate())));

				response = returnResInstance(HttpStatus.OK, "Data source types returned successfully.",
						dsTypesInfo);
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

	@ApiOperation(value = "List Datasource Types", response = Json.class)
	@GetMapping("/api/v1/datasourcetypes/{dataSourceType}")
	@ApiResponses(
			value = {@ApiResponse(code = 200, message = "Data source types returned successfully."),
					@ApiResponse(code = 400, message = "Failed to fetch the datasource types."),
					@ApiResponse(code = 500, message = "Internal error occured.")})
	public ResponseEntity<Object> getDataSourceTypes(
			@PathVariable(value = "dataSourceType") final String dataSourceType) {
		ResponseEntity<Object> response = null;
		try {
			dbUtil.changeSchema("public");

			List<QsDatasourceTypes> dsTypes = datasourceTypeService.getDataSourceTypes(dataSourceType);
			if (dsTypes != null && !dsTypes.isEmpty()) {

				List<DatasourceTypesInfo> dsTypesInfo = new ArrayList<>();
				dsTypes.stream()
				.forEach((dsType) -> dsTypesInfo.add(new DatasourceTypesInfo(dsType.getDataSourceId(),
						dsType.getDataSourceType(), dsType.getDataSourceName(), dsType.getDataSourceImage(),
						dsType.isActive(), dsType.getCreationDate())));

				response = returnResInstance(HttpStatus.OK, "Data source types returned successfully.",
						dsTypesInfo);
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

	@ApiOperation(value = "Execute Pipeline", response = Json.class)
	@PostMapping("/api/v1/pipeline/executepipeline/{projectId}/{userId}/{pipelineId}")
	@ApiResponses(
			value = {@ApiResponse(code = 200, message = "Pipeline execute successfully."),
					@ApiResponse(code = 400, message = "Failed to Execute Pipeline"),
					@ApiResponse(code = 500, message = "Internal error occured.")})
	public ResponseEntity<Object> executePipeline(
			@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId,
			@PathVariable(value = "pipelineId") final int pipelineId) {
		ResponseEntity<Object> response = null;
		try {
			controllerHelper.startExecutePipeline(projectId, userId, pipelineId);
			response = returnResInstance(HttpStatus.OK, "Pipeline execution started.",
					null);

		} catch (final Exception e) {
			log.error("Error {} ", e.getMessage());
			response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
		}
		return response;
	}

}
