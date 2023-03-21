package ai.quantumics.api.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;

import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.adapter.ConnectorsFactory;
import ai.quantumics.api.adapter.DatabaseAdapter;
import ai.quantumics.api.constants.AuditEventType;
import ai.quantumics.api.constants.AuditEventTypeAction;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.ConnectorDetails;
import ai.quantumics.api.model.DatasetMetadata;
import ai.quantumics.api.model.DatasetSchema;
import ai.quantumics.api.model.MetadataReference;
import ai.quantumics.api.model.Pipeline;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFiles;
import ai.quantumics.api.model.QsFolders;
import ai.quantumics.api.model.QsFoldersPiiData;
import ai.quantumics.api.model.QsUserV2;
import ai.quantumics.api.req.ConnectorProperties;
import ai.quantumics.api.req.DataBaseRequest;
import ai.quantumics.api.req.PiiData;
import ai.quantumics.api.req.UploadFileRequest;
import ai.quantumics.api.service.DataSourceService;
import ai.quantumics.api.service.DatasetMetadataService;
import ai.quantumics.api.service.DatasetSchemaService;
import ai.quantumics.api.service.FolderPiiDataService;
import ai.quantumics.api.service.FolderService;
import ai.quantumics.api.service.IngestPipelineService;
import ai.quantumics.api.service.MetadataReferenceService;
import ai.quantumics.api.service.PipelineService;
import ai.quantumics.api.util.CustomResultSetHelperService;
import ai.quantumics.api.util.DbSessionUtil;
import ai.quantumics.api.util.QsUtil;
import ai.quantumics.api.vo.S3FileUploadResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IngestPipelineServiceImpl implements IngestPipelineService {

	private static final Gson gson = new Gson();

	@Autowired
	private DataSourceService dataSourceService;
	@Autowired
	private PipelineService pipelineService;
	@Autowired
	private FolderService folderService;
	@Autowired
	private ControllerHelper controllerHelper;
	@Autowired
	private DatasetSchemaService datasetSchemaService;
	@Autowired
	private DatasetMetadataService datasetMetadataService;
	@Autowired
	private AwsAdapter awsAdapter;
	@Autowired
	private QsUtil qsUtil;
	@Autowired
	private DbSessionUtil dbUtil;
	@Autowired
	private MetadataReferenceService metadataReferenceService;
	@Autowired
	private FolderPiiDataService folderPiiDataService;

	public boolean savePipeline(DataBaseRequest request, QsUserV2 userObj) {
		boolean isPipelineExists = true;
		int connectorId = request.getConnectorId();
		ConnectorDetails connector = null;
		try {
			log.info("Create new connection if connector is zero {}", connectorId);
			if (connectorId == 0) {
				connector = createConnectorDetails(request);
			} else {
				Optional<ConnectorDetails> existingConn = dataSourceService.getById(connectorId);
				if (existingConn.isPresent()) {
					connector = existingConn.get();
				}
				log.info("Get existing connector {}", connector);
			}
			if (connector != null) {
				isPipelineExists = pipelineService.existsPipelineName(request.getPipelineName());
				log.info("Check if Pipeline name exising already {}", isPipelineExists);
				if (!isPipelineExists) {
					Pipeline pipeline = createPipeline(request, userObj);
					log.info("Created new pipeline {}", pipeline);
					if (pipeline != null) {
						pipeline.setConnectorDetails(connector);
						pipeline = pipelineService.save(pipeline);
						log.info("link Connector details with pipeline {}", pipeline);
					}
				}
			}
		} catch (final Exception e) {
			log.error("Exception Occurred {}", e.getMessage());
		}
		return isPipelineExists;
	}

	public QsFolders savePipelineFolderInfo(Projects project, Pipeline pipeline, DataBaseRequest request,
			QsUserV2 userObj, String withoutExtTableName, String schema, String sqlQuery,
			ConnectorProperties properties) throws Exception {
		String version = "1_0";
		QsFolders qsRootFolder = null;
		JSONArray dbObjects = null;
		Map<String, Object> meta = new HashMap<>();
		String tableName = request.getConnectorType() + "_"
				+ pipeline.getPipelineName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + "_"
				+ withoutExtTableName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + "_" + version;
		log.info("Table name {}", tableName);
		if (!folderService.isFolderNameAvailable(tableName)) {
			qsRootFolder = folderService.getFolderByName(tableName);
			log.info("Folder is alreay exist for this table name {}", qsRootFolder);
		} else {
			qsRootFolder = createFolders(request.getProjectId(), userObj, tableName);
			log.info("Created new folder {}", qsRootFolder);
			String auditMessage = controllerHelper.getAuditMessage(QsConstants.AUDIT_FOLDER_MSG,
					AuditEventTypeAction.CREATE.getAction().toLowerCase());
			controllerHelper.recordAuditEvent(AuditEventType.INGEST, AuditEventTypeAction.CREATE,
					String.format(auditMessage, qsRootFolder.getFolderDisplayName()), null, qsRootFolder.getUserId(),
					request.getProjectId());
		}
		dbUtil.changeSchema(project.getDbSchemaName());
		if (pipeline != null) {
			log.info("Scehama {}", schema);
			log.info("Table {}", request.getTableName());
			DatasetSchema datasetSchema = createDatasetSchema(qsRootFolder, pipeline.getPipelineId(),
					request.getConnectorType(), request.getUserId(), request.getSqlType(), schema, withoutExtTableName,
					sqlQuery);
			dbObjects = getDataObjects(datasetSchema, properties, request.getConnectorType(), withoutExtTableName);
			meta.put("columnMetaDataSet", dbObjects.toList());
			createDatasetMetadata(datasetSchema, version, meta, request.getUserId());
		}
		return qsRootFolder;
	}

	public boolean updatePipelineConnector(DataBaseRequest request, QsUserV2 userObj) {
		boolean bActivate = false;
		try {
			Optional<ConnectorDetails> connector = dataSourceService.getById(request.getConnectorId());
			log.info("Get Connector details {}", connector);

			if (connector.isPresent()) {

				ConnectorDetails existConnect = connector.get();
				existConnect.setConnectorName(request.getConnectorName());
				existConnect.setConnectorConfig(gson.toJson(request.getConnectorConfig()));
				existConnect.setConnectorType(request.getConnectorType());
				existConnect.setModifiedDate(DateTime.now().toDate());
				existConnect.setModifiedBy(request.getUserId());
				dataSourceService.save(existConnect);
				log.info("Update Connector details {}", existConnect);

				Pipeline existPipeline = pipelineService.getPipelineById(request.getPipelineId());
				log.info("Get Pipeline details {}", existPipeline);

				if (existPipeline != null) {

					existPipeline.setPipelineName(request.getPipelineName());
					existPipeline.setPipelineType(request.getPipelineType());
					existPipeline.setModifiedBy(getUserName(userObj.getQsUserProfile().getUserFirstName(),
							userObj.getQsUserProfile().getUserLastName()));
					existPipeline.setModifiedDate(DateTime.now().toDate());
					pipelineService.save(existPipeline);
					log.info("Update Pipeline details {}", existPipeline);
				}
				bActivate = true;
			}
		} catch (final Exception e) {
			bActivate = false;
			log.error("Exception Occurred {}", e.getMessage());
		}
		return bActivate;
	}

	public Map<String, Object> executePipelineService(final Projects project, final QsUserV2 userObj,
			Pipeline pipeline) {
		Connection connection = null;
		JSONArray dbObjects = null;
		Map<String, Object> errorlog = new HashMap<>();
		try {
			log.info("Get Pipeline details {}", pipeline);
			if (pipeline != null) {
				ConnectorDetails connectorDetails = pipeline.getConnectorDetails();
				log.info("Get Connector details {}", connectorDetails);
				ConnectorProperties properties = gson.fromJson(connectorDetails.getConnectorConfig(),
						ConnectorProperties.class);
				log.info("Get Connector properties {}", properties);
				ConnectorsFactory factory = new ConnectorsFactory();
				connection = factory.getConnector(connectorDetails.getConnectorType()).getRDBMSConnection(properties)
						.getConnection();
				if (null != connection) {
					log.info("Connection established successfully");
					List<DatasetSchema> datasetSchemaObj = datasetSchemaService
							.getActiveDatasetSchemaByPipelineId(pipeline.getPipelineId(), true);
					log.info("Get Dataset schema details by using pipeline {} {}", pipeline.getPipelineId(),
							datasetSchemaObj);
					if (datasetSchemaObj != null && datasetSchemaObj.size() > 0) {
						for (DatasetSchema datasetSchema : datasetSchemaObj) {
							QsFolders folder = folderService.getFolder(datasetSchema.getFolderId());
							log.info("Get Dataset Schema for folder {}", folder);
							String version = "1_0";
							String folderName = "";
							Map<String, Object> meta = new HashMap<>();

							Optional<DatasetMetadata> schemametadata = datasetMetadataService
									.getByDatasetMetadata(datasetSchema.getDatasetschemaId());
							log.info("Get dataset metada object {}", schemametadata);
							if (schemametadata.isPresent()) {
								DatasetMetadata datasetmetadata = schemametadata.get();
								dbObjects = getDataObjects(datasetSchema, properties,
										connectorDetails.getConnectorType(), datasetSchema.getObjectName());
								// Check Metadata Versions
								meta.put("columnMetaDataSet", dbObjects.toList());
								log.info("Compare latest schema metadata and current table metadata {} {}",
										gson.toJson(JsonParser.parseString(datasetmetadata.getSourceMetadata())),
										gson.toJson(meta));
								folderName = connectorDetails.getConnectorType() + "_"
										+ pipeline.getPipelineName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + "_"
										+ datasetSchema.getObjectName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
										+ "_" + datasetmetadata.getVersion();
								if (gson.toJson(JsonParser.parseString(datasetmetadata.getSourceMetadata()))
										.equals(gson.toJson(meta))) {
									version = datasetmetadata.getVersion();
								} else {
									version = incrementVersion(datasetmetadata.getVersion());
									folderName = incrementTableVersion(folderName, version);
									QsFolders qsFoldersNew = createFolders(project.getProjectId(), userObj, folderName);
									datasetSchema.setActive(false);
									datasetSchemaService.save(datasetSchema);
									datasetSchema = createDatasetSchema(qsFoldersNew, pipeline.getPipelineId(),
											datasetSchema.getObjectType(), userObj.getUserId(),
											datasetSchema.getSqlType(), datasetSchema.getSchemaName(),
											datasetSchema.getObjectName(), datasetSchema.getSqlScript());
									createDatasetMetadata(datasetSchema, version, meta, userObj.getUserId());
									folder = qsFoldersNew;
								}
							}
							String query = datasetSchema.getSqlScript();
							Optional<QsFoldersPiiData> piidataOpt = folderPiiDataService
									.findByFolderIdAndActive(folder.getFolderId());
							PiiData piiData = null;
							UploadFileRequest uploadFileRequest = null;
							if (piidataOpt.isPresent()) {
								piiData = gson.fromJson(piidataOpt.get().getPiiData(), PiiData.class);
								uploadFileRequest = new UploadFileRequest();
								uploadFileRequest.setDropColumns(piiData.getDropColumns());
								uploadFileRequest.setEncryptPiiColumns(piiData.getEncryptPiiColumns());
								uploadFileRequest.setFolderId(folder.getFolderId());
							}
							List<HashMap<String, String>> listOfmapObject = gson.fromJson(dbObjects.toString(),
									new TypeToken<List<HashMap<String, String>>>() {
									}.getType());
							String[] selectedColumns = null;
							if (piiData != null && !piiData.getDropColumns().isEmpty()) {
								String[] dropColumnList = piiData.getDropColumns().split(",");
								// hevo require to drop ? !c.get("column_name").contains("__hevo__") &&
								listOfmapObject = listOfmapObject.stream().filter(
										c -> !Arrays.stream(dropColumnList).anyMatch(c.get("column_name")::contains))
										.collect(Collectors.toList());
								selectedColumns = listOfmapObject.stream().map(map -> map.get("column_name"))
										.toArray(String[]::new);

							}
							if ("D".equalsIgnoreCase(datasetSchema.getSqlType())) {
								StringBuilder queryToRun = new StringBuilder("SELECT ");
								String result = listOfmapObject.stream().map(map -> map.get("column_name"))
										.collect(Collectors.joining(", "));
								queryToRun.append(result);
								queryToRun.append(" FROM ");
								queryToRun.append(datasetSchema.getObjectName());
								log.info(queryToRun.toString());
								query = queryToRun.toString();
								selectedColumns = null;
							}

							List<List<String>> mapObj = new ArrayList<>();
							listOfmapObject.stream().forEach(map -> {
								List<String> keys = new ArrayList<>(2);
								map.entrySet().forEach(entry -> {
									keys.add(map.get(entry.getKey()));
								});
								mapObj.add(keys);
							});
							ObjectMapper mapper = new ObjectMapper();
							try (ResultSet resultset = connection.prepareStatement(query).executeQuery()) {
								log.info("Get table resultset object {}", resultset);

								errorlog = writeRecords(resultset, project, userObj, folder,selectedColumns,
										mapper.writeValueAsString(listOfmapObject), mapObj, connectorDetails,
										properties, folderName, uploadFileRequest);
								log.info("Get error log/S3 file path info {}", errorlog);
								dbUtil.changeSchema(project.getDbSchemaName());
							}
						}
					} else {
						errorlog.put("Error_Msg", "Dataset schema details not found.");
					}
				} else {
					errorlog.put("Error_Msg", "Connection failed.");
				}
			}
		} catch (final SQLException e) {
			log.error("Exception - partition {}", e.getMessage());
			errorlog.put("Error_Msg", e.getMessage());
		} catch (final Exception e) {
			log.error("Exception Occurred {}", e.getMessage());
			errorlog.put("Error_Msg", e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (final Exception e) {
				log.error(e.getMessage());
			}
		}
		return errorlog;
	}

	public String getUserName(String firstName, String lastName) {
		return (lastName != null) ? (firstName + " " + lastName) : firstName;
	}

	public Map<String, Object> writeRecords(ResultSet result, Projects project, QsUserV2 userObj, QsFolders folder,
			String[] selectedColumns, String dbObjects, List<List<String>> mapObj, ConnectorDetails connectorDetails,
			ConnectorProperties properties, String tableName, UploadFileRequest uploadFileRequest)
			throws SQLException, Exception {
		URL destUrl = null;
		Map<String, Object> errorlog = new HashMap<>();
		S3FileUploadResponse s3FileUploadResp = new S3FileUploadResponse();

		try {
			final String pathSep = "/";
			String partitionRef = qsUtil.randomAlphaNumeric();
			partitionRef = partitionRef.toLowerCase();
			final String qsPartition = folder.getFolderName() + pathSep + partitionRef;
			String userName = controllerHelper.getFullName(userObj.getQsUserProfile());

			try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
					OutputStreamWriter streamWriter = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
					CSVWriter writer = buildCSVWriter(streamWriter)) {
				if (selectedColumns != null) {
					CustomResultSetHelperService resultService = new CustomResultSetHelperService();
					resultService.setDesiredColumns(result, selectedColumns);
					writer.setResultService(resultService);
				}
				writer.writeAll(result, true);
				writer.flush();
				String s3FileUrl = awsAdapter.storeSQLFileIntoS3Async(project.getBucketName() + "-raw", stream,
						qsPartition + pathSep + (tableName + ".csv"), "application/csv");

				s3FileUploadResp.setFileSize(String.valueOf(stream.toByteArray().length));
				s3FileUploadResp.setFileName((tableName + ".csv"));
				s3FileUploadResp.setFileContentType("application/csv");
				s3FileUploadResp.setS3FileUrl(s3FileUrl);
				if (uploadFileRequest != null) {
					uploadFileRequest.setCsvFilePath(s3FileUrl);
				}
//				QsFiles qsFile = fileService.getLatestFileInFolder(project.getProjectId(), userObj.getUserId(), folder.getFolderId());
//				if(qsFile != null) {
//					uploadFileRequest = gson.fromJson(qsFile.getAdditionalInfo(), UploadFileRequest.class); 
//				}

				log.info("Table metadata Object {}", dbObjects.toString());

				QsFiles saveFileInfo = awsAdapter.getQsFiles(project.getProjectId(), folder.getFolderId(), project,
						userName, dbObjects.toString(), userObj.getUserId(), uploadFileRequest, s3FileUploadResp);

				if (s3FileUrl != null) {
					destUrl = new URL(s3FileUrl);
					awsAdapter.savePartition(project.getProjectId(), folder.getFolderId(), partitionRef, folder,
							saveFileInfo, destUrl);
					/*
					 * Update the Rules for the new file based on the existing file which is
					 * associated with the current Folder Id
					 */
					awsAdapter.insertRules(project.getProjectId(), folder.getFolderId(), saveFileInfo.getFileId());
				} else {
					log.error("AWS S3 Fileupload Returned empty Destination URL");
				}

//				List<HashMap<String, String>> listOfmapObject = gson.fromJson(dbObjects.toString(),
//						new TypeToken<List<HashMap<String, String>>>() {
//						}.getType());
//
//				List<List<String>> mapObj = new ArrayList<>();
//				listOfmapObject.stream().forEach(map -> {
//					List<String> keys = new ArrayList<>(2);
//					map.entrySet().forEach(entry -> {
//						keys.add(map.get(entry.getKey()));
//					});
//					mapObj.add(keys);
//				});
				log.info("S3 metadata Object {}", mapObj);
				String bucketLocalName = project.getBucketName() + "-raw", folderName = folder.getFolderName(),
						localDBName = project.getRawDb();
				String s3FileLocation = "s3://" + bucketLocalName + "/" + folderName + "/" + partitionRef;
				if (mapObj != null) {
					dbUtil.changeSchema("public");
					List<MetadataReference> metadataRef = metadataReferenceService
							.getMetadataRefBySourceType(connectorDetails.getConnectorType());
					dbUtil.changeSchema(project.getDbSchemaName());

					awsAdapter.createTableInAthena(mapObj, localDBName, partitionRef, tableName, s3FileLocation,
							metadataRef);
				}

				if (partitionRef != null) {
					awsAdapter.addPartitionToTableInAthena(bucketLocalName, localDBName, tableName, partitionRef);
				}

			}

			errorlog.put("Response", destUrl);

			// Update the Folder Last Updated Date and Last Updated by..
			folder.setModifiedDate(QsConstants.getCurrentUtcDate());
			folder.setModifiedBy(Integer.toString(userObj.getUserId()));
			folderService.saveFolders(folder);
			log.info("Updated folder {}", folder);

			String auditMessage = controllerHelper.getAuditMessage(AuditEventType.INGEST, AuditEventTypeAction.CREATE);
			String notification = controllerHelper.getNotificationMessage(AuditEventType.INGEST,
					AuditEventTypeAction.CREATE);
			controllerHelper.recordAuditEvent(AuditEventType.INGEST, AuditEventTypeAction.CREATE,
					String.format(auditMessage,(tableName + ".csv")), String.format(notification, (tableName + ".csv"), userName),
					userObj.getUserId(), project.getProjectId());

		} catch (final SQLException e) {
			log.error("Exception - writeRecords {}", e.getMessage());
			errorlog.put("Error_Msg", e.getMessage());
		} catch (final Exception e) {
			log.error("Exception - writeRecords {}", e.getMessage());
			errorlog.put("Error_Msg", e.getMessage());
		}
		return errorlog;
	}

	private CSVWriter buildCSVWriter(OutputStreamWriter streamWriter) {
		return new CSVWriter(streamWriter, ',', Character.MIN_VALUE, '"', System.lineSeparator());
	}

	private int latestSchemaId(int folderId) {
		int schemaId = 0;
		try {
			Optional<DatasetSchema> datasetschema = datasetSchemaService.getLatestDatasetSchema(folderId);
			if (datasetschema.isPresent()) {
				DatasetSchema schema = datasetschema.get();
				log.error("datasetschema {}", schema);
				schemaId = schema.getDatasetschemaId();
			}
		} catch (final SQLException e) {
			log.error("Exception - latestSchemaId {}", e.getMessage());
		} catch (final Exception e) {
			log.error("Exception - latestSchemaId {}", e.getMessage());
		}
		return schemaId;
	}

	@Override
	public Map<Integer, Pipeline> getAllPipelinesByFolder() {
		Map<Integer, Pipeline> folderWithPipeline = new LinkedHashMap<>();
		try {
			List<Pipeline> lstPipelines = pipelineService.getAllPipelines();
			lstPipelines.stream().forEach(pipeline -> {
				List<DatasetSchema> lstSchema = pipeline.getDatasetSchema();
				lstSchema.stream().forEach(schema -> {
					folderWithPipeline.put(schema.getFolderId(), pipeline);
				});
			});
		} catch (final Exception e) {
			log.error("Exception - getAllPipelinesByFolder {}", e.getMessage());
		}
		log.info("getAllPipelinesByFolder {}", folderWithPipeline);
		return folderWithPipeline;
	}

	private String incrementVersion(String version) {
		String updatedVersion = String.format("%.1f", (Float.parseFloat(version.replaceAll("_", ".")) + 0.1f));
		return updatedVersion.replace(".", "_");
	}

	private String incrementTableVersion(String tableName, String version) {
		int j = tableName.lastIndexOf("_");
		int i = tableName.lastIndexOf("_", j - 1);
		return tableName.substring(0, i) + "_" + version;
	}

	private ConnectorDetails createConnectorDetails(DataBaseRequest request) throws Exception {
		ConnectorDetails connector = new ConnectorDetails();
		connector.setConnectorName(request.getConnectorName());
		connector.setConnectorConfig(gson.toJson(request.getConnectorConfig()));
		connector.setConnectorType(request.getConnectorType());
		connector.setProjectId(request.getProjectId());
		connector.setActive(true);
		connector.setCreatedDate(DateTime.now().toDate());
		connector.setCreatedBy(request.getUserId());
		connector = dataSourceService.save(connector);
		log.info("Created connector {}", connector);
		return connector;
	}

	private Pipeline createPipeline(DataBaseRequest request, QsUserV2 userObj) throws Exception {
		Pipeline pipeline = new Pipeline();
		pipeline.setPipelineName(request.getPipelineName());
		pipeline.setPipelineType(request.getConnectorType());
		pipeline.setPipelineStatus(1);
		pipeline.setActive(true);
		pipeline.setCreatedDate(DateTime.now().toDate());
		pipeline.setCreatedBy(getUserName(userObj.getQsUserProfile().getUserFirstName(),
				userObj.getQsUserProfile().getUserLastName()));
		pipeline = pipelineService.save(pipeline);
		return pipeline;
	}

	private QsFolders createFolders(int projectId, QsUserV2 userObj, String tableName) throws Exception {
		QsFolders folder = new QsFolders();
		folder.setCreatedDate(QsConstants.getCurrentUtcDate());
		folder.setCreatedBy(Integer.toString(userObj.getUserId()));
		folder.setActive(true);
		folder.setFolderDisplayName(tableName);
		folder.setFolderName(tableName);
		folder.setExternal(true);
		folder.setUserId(userObj.getUserId());
		folder.setProjectId(projectId);
		folder.setDataOwner(getUserName(userObj.getQsUserProfile().getUserFirstName(),
				userObj.getQsUserProfile().getUserLastName()));
		folder = folderService.saveFolders(folder);
		return folder;
	}

	private DatasetSchema createDatasetSchema(QsFolders qsFolder, int pipelineId, String connectorType, int userId,
			String sqlType, String schema, String tableName, String sqlQuery) throws Exception {
		DatasetSchema datasetSchema = new DatasetSchema();
		datasetSchema.setFolderId(qsFolder.getFolderId());
		datasetSchema.setPipelineId(pipelineId);
		datasetSchema.setSqlScript(sqlQuery);
		datasetSchema.setObjectName(tableName);
		datasetSchema.setObjectType(connectorType);
		datasetSchema.setDatasetProperties("");
		datasetSchema.setSchemaName(schema);
		datasetSchema.setCreatedDate(QsConstants.getCurrentUtcDate());
		datasetSchema.setCreatedBy(userId);
		datasetSchema.setSqlType(sqlType);
		datasetSchema.setActive(true);
		datasetSchema = datasetSchemaService.save(datasetSchema);
		log.info("Create new Schema for this pipeline {}", datasetSchema);
		return datasetSchema;
	}

	private DatasetSchema createDatasetMetadata(DatasetSchema datasetSchema, String version, Map<String, Object> meta,
			int userId) throws Exception {
		DatasetMetadata metadata = new DatasetMetadata();
		metadata.setDatasetschemaId(datasetSchema.getDatasetschemaId());
		metadata.setSourceMetadata(gson.toJson(meta));
		metadata.setUiMetadata("");
		metadata.setTargetMetadata("");
		metadata.setVersion(version);
		metadata.setCreatedDate(QsConstants.getCurrentUtcDate());
		metadata.setCreatedBy(userId);
		datasetSchema.setDatasetMetadata(metadata);
		datasetSchemaService.save(datasetSchema);
		log.info("Save Metadata for this schema {}", datasetSchema);
		return datasetSchema;
	}

	private JSONArray getDataObjects(DatasetSchema datasetSchema, ConnectorProperties properties, String connectorType,
			String tableName) throws Exception {
		JSONArray dbObjects = null;
		PreparedStatement preparedStatement = null;
		if ("C".equalsIgnoreCase(datasetSchema.getSqlType())) {
			preparedStatement = DatabaseAdapter.getPreparedStatement(properties, connectorType, "custom",
					datasetSchema.getSchemaName(), tableName, datasetSchema.getSqlScript());
			dbObjects = DatabaseAdapter.getDatabaseMetadata(preparedStatement);
		} else {
			preparedStatement = DatabaseAdapter.getPreparedStatement(properties, connectorType, "metadata",
					datasetSchema.getSchemaName(), tableName, datasetSchema.getSqlScript());

			dbObjects = DatabaseAdapter.getDatabaseObjects(preparedStatement);
		}
		log.info("Query {}", preparedStatement.toString());
		log.info("Table metadata Object {}", dbObjects.toList());
		return dbObjects;
	}

}
