package ai.quantumics.api.controller;

import ai.quantumics.api.constants.PipelineStatusCode;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.helper.FolderHelper;
import ai.quantumics.api.model.*;
import ai.quantumics.api.service.*;
import ai.quantumics.api.util.DbSessionUtil;
import ai.quantumics.api.util.ValidatorUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import java.sql.SQLException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/pipeline")
@Api(value = "QuantumSpark SQL Connector API ")
public class PipelineController {

	@Autowired
	private PipelineService pipelineService;

	@Autowired
	private DbSessionUtil dbUtil;

	@Autowired
	private IngestPipelineService ingestPipelineService;

	@Autowired
	private PipelineTranscationService pipelineTranscationService;
	@Autowired
	private ValidatorUtils validatorUtils;
	@Autowired
	private DatasetSchemaService datasetSchemaService;
	@Autowired
	private FolderHelper folderHelper;
	@Autowired
	private FileService fileService;

	@ApiOperation(value = "Get ingested piplelines", response = Json.class)
	@GetMapping("/all/{projectId}/{userId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Ingested all piplines") })
	public ResponseEntity<Object> getAllIngestedPipelines(@PathVariable(value = "projectId") final int projectId,
														  @PathVariable(value = "userId") final int userId) throws Exception {
		final Map<String, Object> response = new HashMap<>();
		List<Pipeline> ingestPipeline = null;
		dbUtil.changeSchema("public");
		validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		ingestPipeline = pipelineService.getAllPipelines();
		log.info("All pipelines {}", ingestPipeline);

		if (CollectionUtils.isNotEmpty(ingestPipeline)) {
			ingestPipeline.forEach(pipeline -> {
				try {
					List<PipelineTranscationDetails> transaction = pipelineTranscationService.getByPipelineTranscations(pipeline.getPipelineId());
					if (CollectionUtils.isNotEmpty(transaction)) {
						PipelineTranscationDetails pipelineTransactionDetails = Collections.max(transaction,
								Comparator.comparing(transactionDetail -> transactionDetail.getExecutionDate()));
						pipeline.setTransactionStatus(pipelineTransactionDetails.getTranscationStatus());
						pipeline.setExecutionDate(pipelineTransactionDetails.getExecutionDate());
					}
				} catch (SQLException e) {
					log.error("Error while fetching pipeline transaction " + e.getMessage());
				}
			});

		}
		response.put("code", 200);
		response.put("result", ingestPipeline);
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Get Connected pipleline Info", response = Json.class)
	@GetMapping("/connector/{projectId}/{userId}/{pipelineId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Connected pipline details") })
	public ResponseEntity<Object> getConnectedPipeline(@PathVariable(value = "projectId") final int projectId,
													   @PathVariable(value = "userId") final int userId, @PathVariable(value = "pipelineId") final int pipelineId)
			throws Exception {
		final Map<String, Object> response = new HashMap<>();
		Pipeline ingestPipeline = null;
		dbUtil.changeSchema("public");
		validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		ingestPipeline = pipelineService.getPipelineById(pipelineId);
		log.info("Connected pipeline {}", ingestPipeline);

		if (ingestPipeline != null) {
			response.put("code", 200);
			response.put("result", ingestPipeline);
		} else {
			response.put("code", 400);
			response.put("message", "Failed to fetch the Pipeline connector details.");
		}

		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Delete pipleline", response = Json.class)
	@DeleteMapping("/delete/{projectId}/{userId}/{pipelineId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Delete Pipline") })
	public ResponseEntity<Object> deletePipeline(@PathVariable(value = "projectId") final int projectId,
												 @PathVariable(value = "userId") final int userId, @PathVariable(value = "pipelineId") final int pipelineId)
			throws Exception {
		final Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		QsUserV2 userObj = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		Pipeline exisPipeline = validatorUtils.checkPipeline(pipelineId);
		log.info("Deleting pipeline info {}", exisPipeline);
		if (exisPipeline != null) {
			exisPipeline.setActive(false);
			exisPipeline.setModifiedBy(
					userObj.getQsUserProfile().getUserFirstName() + " " + userObj.getQsUserProfile().getUserLastName());
			exisPipeline.setModifiedDate(DateTime.now().toDate());
			exisPipeline.getDatasetSchema().stream().forEach(dataSetSchema -> {
				dataSetSchema.getQsFolders().setActive(false);;
				dataSetSchema.setActive(false);
				dbUtil.changeSchema(project.getDbSchemaName());
				folderHelper.deleteFolderContents(project, userObj, dataSetSchema.getQsFolders());
			});
			dbUtil.changeSchema(project.getDbSchemaName());
			Pipeline savedPipeline = pipelineService.save(exisPipeline);
			log.info("deactive pipeline {}", savedPipeline);
			if (savedPipeline != null) {
				response.put("code", 200);
				response.put("massage", "Pipeline deleted successfully.");
			} else {
				response.put("code", 400);
				response.put("massage", "Failed to delete pipeline.");
				return ResponseEntity.status(400).body(response);
			}
		}
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Execute pipleline Info", response = Json.class)
	@PostMapping("/executepipeline/{projectId}/{userId}/{pipelineId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Process pipline details") })
	public ResponseEntity<Object> processPipeline(@PathVariable(value = "projectId") final int projectId,
												  @PathVariable(value = "userId") final int userId, @PathVariable(value = "pipelineId") final int pipelineId)
			throws Exception {

		final Map<String, Object> response = new HashMap<>();

		dbUtil.changeSchema("public");
		QsUserV2 userObj = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);

		dbUtil.changeSchema(project.getDbSchemaName());
		Pipeline pipeline = validatorUtils.checkPipeline(pipelineId);
		PipelineTranscationDetails trans = new PipelineTranscationDetails();
		trans.setPipelineId(pipeline.getPipelineId());
		trans.setPipelineStatus(PipelineStatusCode.RUNNING.getStatus());
		trans.setExecutionDate(QsConstants.getCurrentUtcDate());
		PipelineTranscationDetails newTrans = pipelineTranscationService.savePipelineTranscations(trans);
		log.info("Started Pipeline transcation: {}", newTrans);

		Map<String, Object> errorMesg = ingestPipelineService.executePipelineService(project, userObj, pipeline);
		log.info("Pipeline execution info: {}", errorMesg);

		log.info("Pipeline transcation response: {}", errorMesg.get("Response"));
		Object error = errorMesg.get("Error_Msg");
		log.info("Pipeline transcation error: {}", error);

		if (errorMesg != null && error != null) {
			response.put("code", 400);
			response.put("message", "Failed to execute piplines.");
			newTrans.setPipelineStatus(PipelineStatusCode.FAILED.getStatus());
			newTrans.setPipelineLog(error.toString());
		} else {
			response.put("code", 200);
			response.put("message", "Pipelines executed successfully!");
			newTrans.setPipelineStatus(PipelineStatusCode.SUCCESS.getStatus());
		}
		pipelineTranscationService.savePipelineTranscations(trans);
		log.info("Completed file upload request on: {}", QsConstants.getCurrentUtcDate());
		return ResponseEntity.ok().body(response);

	}

	@ApiOperation(value = "Get pipleline transcation details", response = Json.class)
	@GetMapping("/transcationdetails/{projectId}/{userId}/{pipelineId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pipline transcation details") })
	public ResponseEntity<Object> getPipelineTranscationDetails(@PathVariable(value = "projectId") final int projectId,
																@PathVariable(value = "userId") final int userId, @PathVariable(value = "pipelineId") final int pipelineId)
			throws Exception {
		final Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		List<PipelineTranscationDetails> transcation = pipelineTranscationService.getByPipelineTranscations(pipelineId);
		log.info("Pipeline transcation details: {}", transcation);

		if (transcation != null && transcation.size() > 0) {
			response.put("code", 200);
			response.put("result", transcation);
			response.put("message", "Pipeline Transcation details fetched successfully");
		} else {
			response.put("code", 400);
			response.put("message", "Failed fetched Pipeline Transcation details");
		}
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Get pipleline folder details", response = Json.class)
	@GetMapping("/folderDetails/{projectId}/{userId}/{pipelineId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pipline folder details") })
	public ResponseEntity<Object> getPipelineFolderDetails(@PathVariable(value = "projectId") final int projectId,
														   @PathVariable(value = "userId") final int userId, @PathVariable(value = "pipelineId") final int pipelineId)
			throws Exception {
		final Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		List<DatasetSchema> schemaList = datasetSchemaService.getDatasetSchemaByPipelineId(pipelineId);
		log.info("Pipeline schemaList details: {}", schemaList.size());
		schemaList.stream().forEach(dataSetSchema -> {
			dataSetSchema.getQsFolders().setFilesCount(fileService.getFileCount(dataSetSchema.getQsFolders().getFolderId()));
		});


		if (schemaList != null && schemaList.size() > 0) {
			response.put("code", 200);
			response.put("result", schemaList);
			response.put("message", "Pipeline Schema details fetched successfully");
		} else {
			response.put("code", 400);
			response.put("message", "Failed to fetch Pipeline Schema details");
		}
		return ResponseEntity.ok().body(response);
	}

}
