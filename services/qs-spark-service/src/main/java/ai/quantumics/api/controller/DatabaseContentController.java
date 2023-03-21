/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.adapter.Connectors;
import ai.quantumics.api.adapter.ConnectorsFactory;
import ai.quantumics.api.adapter.DatabaseAdapter;
import ai.quantumics.api.model.ConnectorDetails;
import ai.quantumics.api.model.Pipeline;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFolders;
import ai.quantumics.api.model.QsFoldersPiiData;
import ai.quantumics.api.model.QsUserV2;
import ai.quantumics.api.req.ConnectorProperties;
import ai.quantumics.api.req.DataBaseRequest;
import ai.quantumics.api.req.PiiData;
import ai.quantumics.api.service.DataSourceService;
import ai.quantumics.api.service.FolderPiiDataService;
import ai.quantumics.api.service.FolderService;
import ai.quantumics.api.service.IngestPipelineService;
import ai.quantumics.api.service.PipelineService;
import ai.quantumics.api.util.DbSessionUtil;
import ai.quantumics.api.util.ValidatorUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@RestController
@RequestMapping("/api/v1/db")
@Api(value = "QuantumSpark DataBase Connector API ")
public class DatabaseContentController {

	private static final Gson gson = new Gson();

	@Value("${tmp.ext.location}")
	private String extLocation;

	@Autowired
	private AwsAdapter awsAdapter;
	@Autowired
	private DbSessionUtil dbUtil;
	@Autowired
	private IngestPipelineService ingestPipelineService;
	@Autowired
	private PipelineService pipelineService;
	@Autowired
	private FolderService folderService;
	@Autowired
	private FolderPiiDataService folderPiiDataService;
	@Autowired
	private ValidatorUtils validatorUtils;
	@Autowired
	private DataSourceService dataSourceService;

	@PostMapping("/test-connect")
	@ApiOperation(value = "Test database connection", response = Json.class, notes = "Test Database connection")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Connected successfully!"),
			@ApiResponse(code = 404, message = "Connection Failed") })
	public ResponseEntity<Object> connectTestDataSource(@RequestBody final DataBaseRequest request) throws Exception {
		Connection connection = null;
		Connectors connector = null;
		Map<String, Object> message = new HashMap<>();
		ConnectorsFactory factory = new ConnectorsFactory();
		try {
			ConnectorProperties properties = gson.fromJson(gson.toJsonTree(request.getConnectorConfig()),
					ConnectorProperties.class);
			connector = factory.getConnector(request.getConnectorType());
			connection = connector.getRDBMSConnection(properties).getConnection();
			if (null != connection) {
				message = createSuccessMessage("Connected successfully!");
				message.put("result", true);
			} else {
				message = failureMessage(400, "Connection Failed");
			}
		} finally {
			if (null != connection)
				connection.close();
			connector = null;
		}
		return ResponseEntity.ok().body(message);
	}

	@ApiOperation(value = "List existing Connectors", response = Json.class)
	@GetMapping("/connectorList/{projectId}/{userId}/{connectorType}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Connector list successfully read"),
			@ApiResponse(code = 400, message = "request was not readable !"),
			@ApiResponse(code = 404, message = "Connector list Not found!") })
	public ResponseEntity<Object> getConnectorList(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId,
			@PathVariable(value = "connectorType") final String connectorType) throws Exception {

		final Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		try {
			List<ConnectorDetails> connectorList = dataSourceService.getByConnectorType(connectorType);
			response.put("status", HttpStatus.OK);
			response.put("message", "Connector list successfully read");
			response.put("result", connectorList);

		} catch (final Exception e) {
			response.put("code", HttpStatus.NOT_FOUND);
			response.put("message", e.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/savePipeline")
	@ApiOperation(value = "save database Pipeline", response = Json.class, notes = "save database Pipeline")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Save the database Pipeline"),
			@ApiResponse(code = 404, message = "Not found") })
	public ResponseEntity<Object> saveDatabasePipeline(@RequestBody final DataBaseRequest request) throws Exception {
		Map<String, Object> message = new HashMap<>();
		dbUtil.changeSchema("public");
		QsUserV2 userObj = validatorUtils.checkUser(request.getUserId());
		Projects project = validatorUtils.checkProject(request.getProjectId());
		dbUtil.changeSchema(project.getDbSchemaName());

		if (ingestPipelineService.savePipeline(request, userObj)) {
			message = failureMessage(400, "Pipeline name already exists!");
			return ResponseEntity.ok().body(message);
		}
		Pipeline pipeline = pipelineService.getPipelineByName(request.getPipelineName());

		if (pipeline != null) {
			message = createSuccessMessage("schema list");
			// message.put("result", dbObjects.toList());
			message.put("pipelineId", pipeline.getPipelineId());
			message.put("projectId", project.getProjectId());
			message.put("connectionId", pipeline.getConnectorDetails().getConnectorId());
		} else {
			message = failureMessage(400, "Invalid Db Type");
		}
		return ResponseEntity.ok().body(message);
	}

	@PostMapping("/getdetails/{action}")
	@ApiOperation(value = "Get tables/schema/metadata from database", response = Json.class, notes = "Tables/Schema/Metadata from database")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Listed all tables/schema/metadata from database"),
			@ApiResponse(code = 404, message = "Not found") })
	public ResponseEntity<Object> getTablesFromDataSource(@PathVariable String action,
			@RequestBody final DataBaseRequest request) throws Exception {
		JSONArray dbObjects = null;
		Map<String, Object> message = new HashMap<>();
		dbUtil.changeSchema("public");
		validatorUtils.checkUser(request.getUserId());
		Projects project = validatorUtils.checkProject(request.getProjectId());
		dbUtil.changeSchema(project.getDbSchemaName());
		Optional<ConnectorDetails> connectionDeOptional = dataSourceService.getById(request.getConnectorId());
		if (connectionDeOptional.isPresent()) {
			ConnectorDetails connectorDetails = connectionDeOptional.get();
			ConnectorProperties properties = gson.fromJson(connectorDetails.getConnectorConfig(),
					ConnectorProperties.class);
			PreparedStatement preparedStatement = DatabaseAdapter.getPreparedStatement(properties,
					connectorDetails.getConnectorType(), action, request.getSchemaName(), request.getTableName(), "");
			dbObjects = DatabaseAdapter.getDatabaseObjects(preparedStatement);
			if (dbObjects != null) {
				message = createSuccessMessage(action + " list");
				message.put("result", dbObjects.toList());
			} else {
				message = failureMessage(400, "Invalid Db Type");
			}
		} else {
			throw new Exception("Invalid connector Id");
		}

		return ResponseEntity.ok().body(message);
	}

	@ApiOperation(value = "Identify PII information from selected table", response = Json.class, notes = "Identify PII information from selected table")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "PII information suggestion provided successfully"),
			@ApiResponse(code = 404, message = "PII information suggestion failed") })
	@PostMapping("/identifyPII")
	public ResponseEntity<Object> identifyPII(@RequestBody final DataBaseRequest request) throws Exception {
		Map<String, Object> message = new HashMap<>();
		List<Map<String, String>> resultObj = new ArrayList<>();
		QsFolders qsRootFolder = null;
		File tmpJsonFile = null;

		try {
			String tableName = request.getTableName(), queries = request.getSql();
			String[] tables = (tableName != null ? tableName.split(";") : null);
			dbUtil.changeSchema("public");
			QsUserV2 userObj = validatorUtils.checkUser(request.getUserId());
			Projects project = validatorUtils.checkProject(request.getProjectId());
			dbUtil.changeSchema(project.getDbSchemaName());
			Pipeline pipeline = validatorUtils.checkPipeline(request.getPipelineId());
			request.setPipelineId(pipeline.getPipelineId());
			ConnectorProperties properties = null;
			Optional<ConnectorDetails> connectionDeOptional = dataSourceService.getById(request.getConnectorId());
			if (connectionDeOptional.isPresent()) {
				ConnectorDetails connectorDetails = connectionDeOptional.get();
				request.setConnectorType(connectorDetails.getConnectorType());
				properties = gson.fromJson(connectorDetails.getConnectorConfig(), ConnectorProperties.class);
			} else {
				throw new Exception("Invalid Connector Id");
			}

			log.info("Commencing data download for {}", tableName);
			for (int i = 0; i < tables.length; i++) {

				String strTableName = tables[i];
				String sql[] = queries.split(";");
				String schema[] = request.getSchemaName() != null ? request.getSchemaName().split(";") : "".split(";");
				log.info("Table Name : {}", strTableName);
				log.info("sql Queries : {}", sql.toString());

				Map<String, String> finalRes = new HashMap<>();

				qsRootFolder = ingestPipelineService.savePipelineFolderInfo(project, pipeline, request, userObj,
						strTableName, schema[i], sql[i], properties);
				log.info("Folder object from pipeline {}", qsRootFolder);

				String jsonFilePath = String.format("%s/%s/%s/%s/%s.json", System.getProperty("java.io.tmpdir"),
						project.getProjectId(), request.getUserId(), qsRootFolder.getFolderId(), strTableName);
				log.info("Json File to be created  {}", jsonFilePath);

				if (DatabaseAdapter.exportJSONFile(sql[i], jsonFilePath, request, properties, 500)) {

					String piiColumnsInfo = awsAdapter.getPiiColumnInfo(jsonFilePath);
					piiColumnsInfo = piiColumnsInfo.replaceAll("'", "\"");

					finalRes.put("TABLE_NAME", tables[i]);
					finalRes.put("PII_COLUMNS_INFO", piiColumnsInfo);
					finalRes.put("Folder_Id", String.valueOf(qsRootFolder.getFolderId()));
					finalRes.put("code", "200");
					finalRes.put("message", "File validation completed!");
					resultObj.add(finalRes);
				}
			}
			message.put("pipelineId", pipeline.getPipelineId());
			message.put("projectId", project.getProjectId());
			message.put("result", resultObj);
			message.put("connectorId", request.getConnectorId());
			return ResponseEntity.ok().body(message);

		} finally {
			if (tmpJsonFile != null && tmpJsonFile.exists()) {
				tmpJsonFile.delete();
			}
		}
	}

	@ApiOperation(value = "Preview table content", response = Json.class, notes = "Preview table content")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Get the table preview"),
			@ApiResponse(code = 404, message = "Failed to preview the table") })
	@PostMapping("/preview")
	public ResponseEntity<Object> tablePreview(@RequestBody final DataBaseRequest request) throws Exception {
		Map<String, Object> message = new HashMap<>();
		JSONArray objectList = new JSONArray();
		dbUtil.changeSchema("public");
		validatorUtils.checkUser(request.getUserId());
		Projects project = validatorUtils.checkProject(request.getProjectId());
		ConnectorProperties properties = null;
		dbUtil.changeSchema(project.getDbSchemaName());
		Optional<ConnectorDetails> connectionDeOptional = dataSourceService.getById(request.getConnectorId());
		if (connectionDeOptional.isPresent()) {
			ConnectorDetails connectorDetails = connectionDeOptional.get();
			request.setConnectorType(connectorDetails.getConnectorType());
			properties = gson.fromJson(connectorDetails.getConnectorConfig(), ConnectorProperties.class);
		} else {
			throw new Exception("Invalid Connector Id");
		}
		log.info("Get Connector properties {}", properties);
		dbUtil.changeSchema(request.getSchemaName());
		objectList = DatabaseAdapter.getDatabaseObjects(properties, request.getConnectorType(), request.getSql(), 500);
		dbUtil.changeSchema(project.getDbSchemaName());
		log.info("Data base table Object {}", objectList.toList());
		if (objectList.isEmpty()) {
			message.put("code", 400);
			message.put("message", "No Records exists.");
			return ResponseEntity.ok().body(message);
		} else {
			message.put("code", 200);
			message.put("result", objectList.toList());
			return ResponseEntity.ok().body(message);
		}

	}

	@ApiOperation(value = "Complete Ingestion Pipeline", response = Json.class)
	@PostMapping(value = "/completeDBPipeline", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pipeline Completed successfully"),
			@ApiResponse(code = 400, message = "Malformed or Invalid Content!") })
	public ResponseEntity<Object> completeDBPipeline(@RequestBody final DataBaseRequest request) throws Exception {
		log.info("Received projectId - {} and folderId {}", request.getProjectId());
		Map<String, Object> message = new HashMap<>();
		dbUtil.changeSchema("public");
		validatorUtils.checkUser(request.getUserId());
		Projects project = validatorUtils.checkProject(request.getProjectId());
		dbUtil.changeSchema(project.getDbSchemaName());
		Pipeline pipeline = validatorUtils.checkPipeline(request.getPipelineId());
		log.info("Get Pipeline by using pipelineId {}", pipeline);
		pipeline.setPublished(true);
		pipelineService.save(pipeline);

		for (PiiData piiData : request.getPiiData()) {
			QsFolders folder = folderService.getFolder(piiData.getFolderId());
			List<QsFoldersPiiData> folderPiiDataList = folderPiiDataService.findByFolderId(folder.getFolderId());
			for (QsFoldersPiiData qsFoldersPiiData : folderPiiDataList) {
				qsFoldersPiiData.setActive(false);
				folderPiiDataService.saveFoldersPiiData(qsFoldersPiiData);
			}
			QsFoldersPiiData qsFoldersPiiData = new QsFoldersPiiData();
			qsFoldersPiiData.setFolderId(folder.getFolderId());
			qsFoldersPiiData.setPiiData(new org.json.JSONObject(piiData).toString());
			qsFoldersPiiData.setActive(true);
			folderPiiDataService.saveFoldersPiiData(qsFoldersPiiData);
		}

		message.put("code", 200);
		message.put("result", "Pipeline completed successfully");
		return ResponseEntity.ok().body(message);
	}

	@ApiOperation(value = "Update pipleline Info", response = Json.class)
	@PostMapping("/updatepipelineinfo")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Update pipline details") })
	public ResponseEntity<Object> updatePipeline(@RequestBody final DataBaseRequest request) throws Exception {
		final Map<String, Object> response = new HashMap<>();

		dbUtil.changeSchema("public");
		QsUserV2 userObj = validatorUtils.checkUser(request.getUserId());
		Projects project = validatorUtils.checkProject(request.getProjectId());
		dbUtil.changeSchema(project.getDbSchemaName());

		if (ingestPipelineService.updatePipelineConnector(request, userObj)) {
			response.put("code", 200);
			response.put("message", "Pipeline updated successfully");
			return ResponseEntity.ok().body(response);
		} else {
			throw new Exception("Pipeline Couldn't be updated");
		}

	}

	private HashMap<String, Object> createSuccessMessage(final String message) {
		final HashMap<String, Object> success = new HashMap<>();
		success.put("code", HttpStatus.OK.value());
		success.put("message", message);
		return success;
	}

	private HashMap<String, Object> failureMessage(final int code, final String message) {
		final HashMap<String, Object> success = new HashMap<>();
		success.put("code", code);
		success.put("message", message);
		return success;
	}

}
