/*

 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.constants.AuditEventType;
import ai.quantumics.api.constants.AuditEventTypeAction;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.*;
import ai.quantumics.api.req.DownloadFileRequest;
import ai.quantumics.api.req.UpdateFileReq;
import ai.quantumics.api.service.*;
import ai.quantumics.api.util.CipherUtils;
import ai.quantumics.api.util.DbSessionUtil;
import ai.quantumics.api.util.ValidatorUtils;
import ai.quantumics.api.vo.QsFileContent;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@Api(value = "QuantumSpark Service API ")
public class FileController {

	private final DbSessionUtil dbUtil;
	private final AwsAdapter awsAdapter;
	private final FileService fileService;
	private final ProjectService projectService;
	private final FolderService folderService;
	private final PartitionService partitionService;
	private final EngFlowService engFlowService;
	private final EngFlowJobService engFlowJobService;
	private final EngFlowMetaDataService engFlowMetaDataService;
	private final RunJobService runJobService;
	private final UserServiceV2 userService;

	private final FileDataProfileService fileDataProfileService;
	private final ControllerHelper controllerHelper;
	private final ProjectCumulativeSizeService projectSizeService;
	private ValidatorUtils validatorUtils;

	public FileController(final DbSessionUtil dbUtilCi, final AwsAdapter awsAdapterCi, final FileService fileServiceCi,
						  final ProjectService projectServiceCi, final FolderService folderServiceCi,
						  final PartitionService partitionServiceCi, final EngFlowService engFlowServiceCi,
						  final EngFlowJobService engFlowJobServiceCi, final EngFlowMetaDataService engFlowMetaDataServiceCi,
						  final RunJobService runJobServiceCi,
						  final UserServiceV2 userServiceCi,
						  ControllerHelper controllerHelperCi,
						  ProjectCumulativeSizeService projectSizeServiceCi, ValidatorUtils validatorUtils,
						  final FileDataProfileService fileDataProfileServiceCi) {
		dbUtil = dbUtilCi;
		awsAdapter = awsAdapterCi;
		fileService = fileServiceCi;
		projectService = projectServiceCi;
		folderService = folderServiceCi;
		partitionService = partitionServiceCi;
		engFlowService = engFlowServiceCi;
		engFlowJobService = engFlowJobServiceCi;
		engFlowMetaDataService = engFlowMetaDataServiceCi;
		runJobService = runJobServiceCi;
		userService = userServiceCi;
		controllerHelper = controllerHelperCi;
		projectSizeService = projectSizeServiceCi;
		fileDataProfileService = fileDataProfileServiceCi;
		this.validatorUtils = validatorUtils;
	}

	@DeleteMapping("/{projectId}/{userId}/{folderId}/{fileId}")
	public ResponseEntity<Object> deleteFile(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "folderId") final int folderId,
			@PathVariable(value = "fileId") final int fileId) throws Exception {
		final Map<String, Object> genericResponse = new HashMap<>();
		dbUtil.changeSchema("public");
		QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());

		try {
			QsFolders folder = folderService.getFolder(folderId);
			if (folder != null) {
				Optional<QsFiles> fileOp = fileService.getFileById(fileId);
				if (fileOp.isPresent()) {
					QsFiles file = fileOp.get();
					String userName = user.getQsUserProfile().getUserFirstName() + " "
							+ user.getQsUserProfile().getUserLastName();
					file.setActive(false);
					fileService.saveFileInfo(file);

					dbUtil.changeSchema("public");
					ProjectCumulativeSizeInfo projectSizeInfo = projectSizeService.getSizeInfoForProject(projectId,
							userId);
					if (projectSizeInfo != null) {
						long currentProjSize = projectSizeInfo.getCumulativeSize();
						projectSizeInfo.setCumulativeSize(currentProjSize - Long.parseLong(file.getFileSize()));

						projectSizeService.save(projectSizeInfo);
					}

					dbUtil.changeSchema(project.getDbSchemaName());
					int fileCount = fileService.getFileCount(folderId);
					boolean emptyFolder = false;

					if (fileCount == 0) {
						emptyFolder = true;
						folder.setColumnHeaders(null);
					}

					folder.setModifiedDate(DateTime.now().toDate());
					folder.setModifiedBy(userName);

					folderService.saveFolders(folder);

					// Now delete the Raw file from S3 and Athena partition as well..
					Optional<QsPartition> partitionOp = partitionService.getPartitionByFileId(folderId, fileId);
					if (partitionOp.isPresent()) {
						QsPartition partition = partitionOp.get();

						// Handle File delete post actions...
						awsAdapter.handlePostFileDeleteActions(project, folderId, folder.getFolderName(), emptyFolder,
								partition.getPartitionName(), file.getFileName());
					}
					String auditMessage = controllerHelper.getAuditMessage(AuditEventType.INGEST,
							AuditEventTypeAction.DELETE);
					String notification = controllerHelper.getNotificationMessage(AuditEventType.INGEST,
							AuditEventTypeAction.DELETE);
					controllerHelper.recordAuditEvent(AuditEventType.INGEST, AuditEventTypeAction.DELETE,
							String.format(auditMessage, file.getFileName()),
							String.format(notification, file.getFileName(), userName), user.getUserId(),
							project.getProjectId());// TODO: Userid must be read from the session
					genericResponse.put("code", HttpStatus.SC_OK);
					genericResponse.put("message", "File deleted successfully.");
				} else {
					genericResponse.put("code", HttpStatus.SC_OK);
					genericResponse.put("message", String.format("No records found for the File Id: %d", fileId));
				}
			} else {
				genericResponse.put("code", HttpStatus.SC_BAD_REQUEST);
				genericResponse.put("message", "Folder with Id: " + folderId + " not found.");
			}
		} catch (Exception exception) {
			genericResponse.put("code", HttpStatus.SC_INTERNAL_SERVER_ERROR);
			genericResponse.put("message",
					String.format("Error while Removing the fileId %d - %s", fileId, exception.getMessage()));
		}
		return ResponseEntity.status(HttpStatus.SC_OK).body(genericResponse);
	}

	@ApiOperation(value = "List file contents", response = Json.class)
	@GetMapping("/content/{projectId}/{userId}/{folderId}/{fileId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "File content successfully read"),
			@ApiResponse(code = 400, message = "File was not readable !"),
			@ApiResponse(code = 404, message = "File Not found!") })
	public ResponseEntity<Object> getFileContent(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "folderId") final int folderId,
			@PathVariable(value = "fileId") final int fileId) throws Exception {

		final Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		try {
			QsFolders folder = folderService.getFolder(folderId);
			if (folder == null) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested folder path not found");

				return ResponseEntity.ok().body(response);
			}

			Optional<QsFiles> fileOp = fileService.getFileById(fileId);
			if (!fileOp.isPresent()) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested file not found");

				return ResponseEntity.ok().body(response);
			}

			QsFiles file = fileOp.get();

			QsFileContent tableDetails = new QsFileContent();
			tableDetails.setProject(project);
			tableDetails.setFileId(fileId);
			tableDetails.setFolderId(folderId);
			tableDetails.setFileName(file.getFileName());
			tableDetails.setFileMetadata(file.getQsMetaData());
			tableDetails.setFolderName(folder.getFolderName());

			// This end point is used only for retrieving the Raw file content. Since, Raw
			// files are stored in the following structure:
			// s3://<BUCKET_NAME>/<FOLDER_NAME>/<PARTION_NAME>/CSV File.

			Optional<QsPartition> partitionOp = partitionService.getPartitionByFileId(folderId, fileId);
			QsPartition partition = null;
			if (partitionOp.isPresent()) {
				partition = partitionOp.get();
			}

			String partitionName = null;
			if (partition != null) {
				partitionName = partition.getPartitionName();
			}
			tableDetails.setPartition(partitionName);

			final ArrayNode responseJson = awsAdapter.getRawFileContentHelper(tableDetails);

			response.put("status", HttpStatus.SC_OK);
			response.put("message", "File content successfully read");
			response.put("data", CipherUtils.encrypt(responseJson.get(0).toPrettyString()));
			response.put("metadata", CipherUtils.encrypt(responseJson.get(1).toPrettyString()));
			response.put("analytics", responseJson.get(2));
			response.put("file_additional_info", file.getAdditionalInfo());
		} catch (final Exception e) {
			response.put("code", HttpStatus.SC_NOT_FOUND);
			response.put("message", e.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Analyze file contents", response = Json.class)
	@GetMapping("/columnanalytics/{projectId}/{folderId}/{fileId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "File content successfully read"),
			@ApiResponse(code = 400, message = "File was not readable !"),
			@ApiResponse(code = 404, message = "File Not found!") })
	public ResponseEntity<Object> getFileColumnAnalytics(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "folderId") final int folderId, @PathVariable(value = "fileId") final int fileId)
			throws Exception {

		final Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		//QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		try {

			QsFolders folder = folderService.getFolder(folderId);
			if (folder == null) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested folder path not found");
			}

			Optional<QsFiles> fileOp = fileService.getFileById(fileId);
			if (!fileOp.isPresent()) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested file not found");
			}

			QsFiles file = fileOp.get();

			QsFileContent tableDetails = new QsFileContent();
			tableDetails.setProject(project);
			tableDetails.setFileId(fileId);
			tableDetails.setFolderId(folderId);
			tableDetails.setFileName(file.getFileName());
			tableDetails.setFileMetadata(file.getQsMetaData());
			tableDetails.setFolderName(folder.getFolderName());

			// This end point is used only for retrieving the Raw file content. Since, Raw
			// files are stored in the following structure:
			// s3://<BUCKET_NAME>/<FOLDER_NAME>/<PARTION_NAME>/CSV File.

			Optional<QsPartition> partitionOp = partitionService.getPartitionByFileId(folderId, fileId);
			QsPartition partition = null;
			if (partitionOp.isPresent()) {
				partition = partitionOp.get();
			}

			String partitionName = null;
			if (partition != null) {
				partitionName = partition.getPartitionName();
			}
			tableDetails.setPartition(partitionName);

			final ArrayNode responseJson = awsAdapter.getRawFileColumnAnalytics(tableDetails);

			response.put("status", HttpStatus.SC_OK);
			response.put("message", "File column analytics successfully read");
			response.put("analytics", responseJson.get(0));
		} catch (final Exception e) {
			response.put("code", HttpStatus.SC_NOT_FOUND);
			response.put("message", e.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "List file contents", response = Json.class)
	@GetMapping("/contentanalytics/{projectId}/{folderId}/{fileId}/{analyticsType}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "File content successfully read"),
			@ApiResponse(code = 400, message = "File was not readable !"),
			@ApiResponse(code = 404, message = "File Not found!") })
	public ResponseEntity<Object> getFileContentAnalytics(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "folderId") final int folderId, @PathVariable(value = "fileId") final int fileId,
			@PathVariable(value = "analyticsType") final String analyticsType) throws Exception{
		final Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		//QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		try {
			QsFolders folder = folderService.getFolder(folderId);
			if (folder == null) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested folder path not found");
			}

			Optional<QsFiles> fileOp = fileService.getFileById(fileId);
			if (!fileOp.isPresent()) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested file not found");
			}

			QsFiles file = fileOp.get();

			Optional<QsPartition> partitionOp = partitionService.getPartitionByFileId(folderId, fileId);
			QsPartition partition = null;
			if (partitionOp.isPresent()) {
				partition = partitionOp.get();
			}

			String fileS3Location = partition.getS3Location();
			String fileObjectKey = null;
			if (fileS3Location != null) {
				URL url = new URL(fileS3Location);
				fileObjectKey = url.getPath();
				fileObjectKey = fileObjectKey.substring(1); // remove the trailing '/' character
				log.info("File S3 location: {}", fileS3Location);
				log.info("File Object key: {}", fileObjectKey);
			}

			QsFileContent fileContent = new QsFileContent();
			fileContent.setProject(project);
			fileContent.setFileId(fileId);
			fileContent.setFolderId(folderId);
			fileContent.setFileName(file.getFileName());
			fileContent.setFileMetadata(file.getQsMetaData());
			fileContent.setFolderName(folder.getFolderName());
			fileContent.setFileS3Location(fileS3Location);
			fileContent.setFileObjectKey(fileObjectKey);
			fileContent.setAnalyticsType(analyticsType);

			String responseJson = awsAdapter.getRawFileContentAnalyticsV2(project.getBucketName() + "-raw",
					fileContent);

			if (responseJson != null) {
				response.put("status", HttpStatus.SC_OK);
				response.put("message", "File content analytics completed successfully.");
				response.put("data", responseJson);
			} else {
				response.put("status", HttpStatus.SC_NOT_FOUND);
				response.put("message", "Failed to perform analytics on the selected file.");
			}

		} catch (final Exception e) {
			response.put("code", HttpStatus.SC_NOT_FOUND);
			response.put("message", e.getMessage());
		}

		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Delta between two files", response = Json.class)
	@GetMapping("/filesdelta/{projectId}/{folderId}/{file1Id}/{file2Id}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "File content successfully read"),
			@ApiResponse(code = 400, message = "File was not readable !"),
			@ApiResponse(code = 404, message = "File Not found!") })
	public ResponseEntity<Object> getFileContentAnalytics(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "folderId") final int folderId, @PathVariable(value = "file1Id") final int file1Id,
			@PathVariable(value = "file2Id") final int file2Id) throws Exception{
		final Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		//QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		try {
			QsFolders folder = folderService.getFolder(folderId);
			if (folder == null) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested folder path not found");
			}

			Optional<QsFiles> file1Op = fileService.getFileById(file1Id);
			if (!file1Op.isPresent()) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested file not found");
			}

			Optional<QsFiles> file2Op = fileService.getFileById(file2Id);
			if (!file2Op.isPresent()) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested file not found");
			}

			String file1ObjectKey = getFileObjectKey(folderId, file1Id);
			String file2ObjectKey = getFileObjectKey(folderId, file2Id);

			String responseJson = awsAdapter.getDeltaBtwnFiles(project.getBucketName() + "-raw", file1ObjectKey,
					file2ObjectKey);

			if (responseJson != null) {
				response.put("status", HttpStatus.SC_OK);
				response.put("message", "Delta between two files computed successfully.");
				response.put("data", responseJson);
			} else {
				response.put("status", HttpStatus.SC_NOT_FOUND);
				response.put("message", "Failed to perform delta computation between the selected files.");
			}

		} catch (final Exception e) {
			response.put("code", HttpStatus.SC_NOT_FOUND);
			response.put("message", e.getMessage());
		}

		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "files", response = Json.class)
	@GetMapping("/{projectId}/{userId}/{folderId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "List All QsFiles for Project ID & Folder ID") })
	public ResponseEntity<Object> getFiles(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "folderId") final int folderId) throws Exception{
		dbUtil.changeSchema("public");
		QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		controllerHelper.getProjects(projectId, userId);
		dbUtil.changeSchema(project.getDbSchemaName());
		final Map<String, Object> response = new HashMap<>();
		log.info("projectId = {} and folderId {}", projectId, folderId);
		response.put("code", HttpStatus.SC_OK);
		response.put("result", fileService.getFiles(projectId, userId, folderId));
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "update a File", response = Json.class, notes = "update a file")
	@PutMapping("{projectId}/{fileId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "File updated successfully"),
			@ApiResponse(code = 404, message = "File Not Found") })
	public ResponseEntity<Object> updateFile(@RequestBody final UpdateFileReq updateFile,
			@PathVariable(value = "projectId") final int projectId, @PathVariable(value = "fileId") final int fileId) throws Exception{
		final Map<String, Object> genericResponse = new HashMap<>();
		dbUtil.changeSchema("public");
		//QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		genericResponse.put("code", HttpStatus.SC_NOT_IMPLEMENTED);
		genericResponse.put("message", "Not Implemented");
		return ResponseEntity.status(HttpStatus.SC_NOT_IMPLEMENTED).body(genericResponse);
	}

	/* S3 based approach */
	@PostMapping("/downloadfile/{userId}")
	public ResponseEntity<Object> downloadFile(@RequestBody DownloadFileRequest downloadFileRequest,
			@PathVariable(value = "userId") final int userId) throws Exception{
		dbUtil.changeSchema("public");
		QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(downloadFileRequest.getProjectId());
		dbUtil.changeSchema(project.getDbSchemaName());
		Map<String, Object> response = null;
		String auditMessage = null;
		AuditEventTypeAction auditEventTypeAction = AuditEventTypeAction.DOWNLOADED;
		AuditEventType auditEventType = null;
		S3Object s3Object = null;

		try {
			String bucketName = project.getBucketName() + "-" + downloadFileRequest.getType();
			String objectKey = null;

			if (QsConstants.RAW.equals(downloadFileRequest.getType())) {
				// TODO: Not yet implemented..

			} else if (QsConstants.PROCESSED.equals(downloadFileRequest.getType())) {
				Optional<RunJobStatus> runJobStatusOp = runJobService.getRunJob(downloadFileRequest.getJobId());
				if (runJobStatusOp.isPresent()) {
					RunJobStatus runJobStatus = runJobStatusOp.get();
					QsFolders folder = folderService.getFolder(runJobStatus.getFolderId());
					objectKey = String.format("%s/%s", folder.getFolderName(), downloadFileRequest.getJobId());

					log.info("Object Key to searched in the bucket: {} is: {}", bucketName, objectKey);
					s3Object = awsAdapter.fetchObject(bucketName, objectKey);
					log.info("Object found in S3 and its key is: {}", s3Object.getKey());
					Optional<QsFiles> filesOp = fileService.getFileById(runJobStatus.getFileId());
					auditEventType = AuditEventType.CLEANSE;
					if (filesOp.isPresent()) {
						auditMessage = controllerHelper.getAuditMessage(auditEventType, auditEventTypeAction);
						auditMessage = String.format(auditMessage, filesOp.get().getFileName());
					}
				}
			} else if (QsConstants.ENG.equals(downloadFileRequest.getType())) {
				Optional<EngFlowJob> engFlowJobOp = engFlowJobService.getThisJob(downloadFileRequest.getJobId());
				EngFlowJob engFlowJob = null;
				if (engFlowJobOp.isPresent()) {
					engFlowJob = engFlowJobOp.get();

					log.info("Eng flow Job found: {}", engFlowJob.getEngFlowId());
					Optional<EngFlow> engFlowOp = engFlowService
							.getFlowsForProjectAndId(downloadFileRequest.getProjectId(), engFlowJob.getEngFlowId());
					EngFlow engFlow = null;
					if (engFlowOp.isPresent()) {
						engFlow = engFlowOp.get();
						EngFlowMetaDataAwsRef engFlowMetaAwsRef = engFlowMetaDataService
								.getByFlowId(engFlow.getEngFlowId());
						if (engFlowMetaAwsRef != null) {
							log.info("Eng flow found: {} and Folder Name: {}", engFlow.getEngFlowName(),
									engFlowMetaAwsRef.getAthenaTable());
							objectKey = String.format("%s/%s", engFlowMetaAwsRef.getAthenaTable(),
									engFlow.getEngFlowName());
							log.info("Object Key to searched in the bucket: {} is: {}", bucketName, objectKey);
							s3Object = awsAdapter.fetchObject(bucketName, objectKey);
							log.info("Object found in S3 and its key is: {}", s3Object.getKey());
							auditEventType = AuditEventType.ENGINEER;
							auditMessage = controllerHelper.getAuditMessage(auditEventType, auditEventTypeAction);
							auditMessage = String.format(auditMessage, engFlow.getEngFlowName());
						}
					}
				}
			}

			if (s3Object != null) {
				String key = s3Object.getKey();
				log.info("S3 Object Key which will be downloaded is: {}", key);
				String preSignedUrl = awsAdapter.generatePresignedUrl(bucketName, key);
				controllerHelper.recordAuditEvent(auditEventType, auditEventTypeAction, auditMessage, null, userId,
						project.getProjectId());

				response = createSuccessMessage(200, "Download request processed.", preSignedUrl);
			} else {
				response = createSuccessMessage(404, "Requested S3 Object '{}' not found.", null);
			}

		} catch (Exception e) {
			log.error("Error downloading the file: {}", e.getMessage());
			response = createSuccessMessage(500, "Download request failed. Internal server error.", null);
		} finally {
			if (s3Object != null) {
				try {
					s3Object.close();
				} catch (IOException e) {

				}
			}
		}

		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Analyze file for data profiling", response = Json.class)
	@GetMapping("/dataprofiling/{projectId}/{userId}/{folderId}/{fileId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "File analyzed successfully"),
			@ApiResponse(code = 400, message = "File was not readable !"),
			@ApiResponse(code = 404, message = "File Not found!") })
	public ResponseEntity<Object> getFileDataProfilingInfo(@PathVariable(value = "projectId") final int projectId,
														   @PathVariable(value = "userId") final int userId, @PathVariable(value = "folderId") final int folderId,
														   @PathVariable(value = "fileId") final int fileId) throws Exception {

		final Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		//QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		try {
			QsFolders folder = folderService.getFolder(folderId);
			if (folder == null) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested folder path not found");
			} else {
				Optional<QsFiles> fileOp = fileService.getFileById(fileId);

				if (!fileOp.isPresent()) {
					response.put("code", HttpStatus.SC_BAD_REQUEST);
					response.put("message", "Requested file not found");
				} else {
					QsFiles file = fileOp.get();

					// Check if the file is already having the data profile information in the DB,
					// if so, return it. Else calculate it..

					FileDataProfilingInfo fileDataProfileInfo = fileDataProfileService
							.getFileDataProfilingInfo(projectId, folderId, fileId);
					if (fileDataProfileInfo != null) {

						String fileDataProfileJson = fileDataProfileInfo.getFileProfilingInfo();

						response.put("status", HttpStatus.SC_OK);
						response.put("message", "File column analytics successfully read");
						response.put("analytics", fileDataProfileJson);
					} else {
						Optional<QsPartition> partitionOp = partitionService.getPartitionByFileId(folderId, fileId);
						QsPartition partition = null;
						if (partitionOp.isPresent()) {
							partition = partitionOp.get();
							String partitionName = partition.getPartitionName();

							String objectKey = "";
							StringBuilder sb = new StringBuilder();
							sb.append(folder.getFolderName());
							sb.append("/");
							sb.append(partitionName);
							sb.append("/");
							sb.append(file.getFileName());

							objectKey = sb.toString();

							log.info("Object Key: {}", objectKey);

							String responseJson = awsAdapter.getFileStatistics(project.getBucketName() + "-raw",
									objectKey);

							if (responseJson != null && !responseJson.isEmpty()) {
								FileDataProfilingInfo newFileDataProfInfo = new FileDataProfilingInfo();
								newFileDataProfInfo.setProjectId(projectId);
								newFileDataProfInfo.setFolderId(folderId);
								newFileDataProfInfo.setFileId(fileId);
								newFileDataProfInfo.setFileProfilingInfo(responseJson);
								newFileDataProfInfo.setActive(true);
								newFileDataProfInfo.setCreationDate(DateTime.now().toDate());

								newFileDataProfInfo = fileDataProfileService.save(newFileDataProfInfo);
								response.put("status", HttpStatus.SC_OK);
								response.put("message", "File column analytics successfully read");
								response.put("analytics", newFileDataProfInfo.getFileProfilingInfo());

							} else {
								response.put("code", HttpStatus.SC_BAD_REQUEST);
								response.put("message",
										"Failed to fetch the file profiling information. Please check the encoding format of the CSV file.");
							}
						} else {
							response.put("code", HttpStatus.SC_BAD_REQUEST);
							response.put("message", "Requested file not found");
						}
					}
				}
			}
		} catch (final Exception e) {
			response.put("code", HttpStatus.SC_NOT_FOUND);
			response.put("message", e.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Fetch the Column Value frequency of a File", response = Json.class)
	@GetMapping("/dataprofiling/{projectId}/{userId}/{folderId}/{fileId}/{columnName}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "File's Column value frequency is fetched successfully"),
			@ApiResponse(code = 400, message = "File was not readable!"),
			@ApiResponse(code = 404, message = "File Not found!") })
	public ResponseEntity<Object> getColumnValueFrequency(@PathVariable(value = "projectId") final int projectId,
														  @PathVariable(value = "userId") final int userId, @PathVariable(value = "folderId") final int folderId,
														  @PathVariable(value = "fileId") final int fileId,
														  @PathVariable(value = "columnName") final String columnName) throws Exception {

		final Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		//QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		try {
			QsFolders folder = folderService.getFolder(folderId);
			if (folder == null) {
				response.put("code", HttpStatus.SC_BAD_REQUEST);
				response.put("message", "Requested folder path not found");
			} else {
				Optional<QsFiles> fileOp = fileService.getFileById(fileId);

				if (!fileOp.isPresent()) {
					response.put("code", HttpStatus.SC_BAD_REQUEST);
					response.put("message", "Requested file not found");
				} else {
					QsFiles file = fileOp.get();

					Optional<QsPartition> partitionOp = partitionService.getPartitionByFileId(folderId, fileId);
					QsPartition partition = null;
					if (partitionOp.isPresent()) {
						partition = partitionOp.get();
						String partitionName = partition.getPartitionName();

						String objectKey = "";
						StringBuilder sb = new StringBuilder();
						sb.append(folder.getFolderName());
						sb.append("/");
						sb.append(partitionName);
						sb.append("/");
						sb.append(file.getFileName());

						objectKey = sb.toString();

						log.info("Object Key: {}", objectKey);

						final String responseJson = awsAdapter.getFileColumnValueFreq(project.getBucketName() + "-raw",
								objectKey, columnName);

						response.put("status", HttpStatus.SC_OK);
						response.put("message", "File's Column value frequency is fetched successfully");
						response.put("analytics", responseJson);
					} else {
						response.put("code", HttpStatus.SC_BAD_REQUEST);
						response.put("message", "Requested file not found");
					}
				}
			}
		} catch (final Exception e) {
			response.put("code", HttpStatus.SC_NOT_FOUND);
			response.put("message", e.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Download decrypted file.", response = Json.class)
	@GetMapping("/download/{projectId}/{userId}/{folderId}/{fileId}/{jobId}/{fileType}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Downloaded file successfully"),
			@ApiResponse(code = 400, message = "Folder or File not found."), })
	public ResponseEntity<Object> downloadDecryptedFile(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "folderId") final int folderId,
			@PathVariable(value = "fileId") final int fileId, @PathVariable(value = "jobId") final String jobId,
			@PathVariable(value = "fileType") final String fileType) throws Exception{
		Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		BufferedWriter bw = null;
		File csvFileTmp = null;
		try {
			QsFolders folder = folderService.getFolder(folderId);
			if (folder != null) {
				Optional<QsFiles> fileOp = fileService.getFileById(fileId);
				QsFiles file = null;
				S3Object s3Object = null;
				if (fileOp.isPresent()) {
					file = fileOp.get();

					String objectKey = String.format("%s/%s", folder.getFolderName(), jobId);
					String bucketName = project.getBucketName() + "-" + fileType;
					log.info("Object Key to searched in the bucket: {} is: {}", bucketName, objectKey);
					s3Object = awsAdapter.fetchObject(bucketName, objectKey);
					log.info("Object found in S3 and its key is: {}", s3Object.getKey());
					if (s3Object != null) {
						// Check if the file has encrypted columns or not.. If so, decrypt it and then
						// download..

						String tmpdir = System.getProperty("java.io.tmpdir");
						String path = s3Object.getKey();
						String tmpCsvFileNameWithoutExt = path.substring(path.lastIndexOf("/") + 1,
								path.lastIndexOf("."));
						String tmpCsvFileName = String.format("%s/%s/%s/tmp/%s.csv", tmpdir, folderId, fileId,
								tmpCsvFileNameWithoutExt);
						csvFileTmp = new File(tmpCsvFileName);
						if (!csvFileTmp.getParentFile().exists()) {
							csvFileTmp.getParentFile().mkdirs();
						}

						log.info("CSV File to be decrypted before download is: {}", csvFileTmp.getAbsolutePath());

						String piiCols = "";
						String additionalInfo = file.getAdditionalInfo();
						ObjectMapper mapper = new ObjectMapper();
						Resource resource = null;

						if (additionalInfo != null) {
							JsonNode additionalInfoNode = mapper.readValue(additionalInfo, JsonNode.class);
							piiCols = additionalInfoNode.get("encryptPiiColumns").asText();
							if (piiCols != null && !piiCols.isEmpty()) {
								CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
								CsvMapper csvMapper = new CsvMapper();
								List<Object> readAllLines = csvMapper.readerFor(Map.class).with(csvSchema)
										.readValues(
												new BufferedReader(new InputStreamReader(s3Object.getObjectContent())))
										.readAll();
								//controllerHelper.decryptColumnsOfCsvFile(readAllLines, piiCols); Commented bug:4827

								CsvSchema.Builder schemaBuilder = CsvSchema.builder();
								CsvSchema schema = null;
								if (readAllLines != null && !readAllLines.isEmpty()) {
									Object obj = readAllLines.get(0);
									Map<String, String> firstItem = (Map<String, String>) obj;
									for (String col : firstItem.keySet()) {
										schemaBuilder.addColumn(col);
									}
									schema = schemaBuilder.build().withLineSeparator("\r").withHeader();
									bw = new BufferedWriter(new FileWriter(csvFileTmp));
									csvMapper.writer(schema).writeValues(bw).writeAll(readAllLines);

									bw.flush();
								}

								Path filePath = Paths.get(csvFileTmp.getAbsolutePath()).toAbsolutePath().normalize();
								resource = new UrlResource(filePath.toUri());
							} else {
								log.info("There are no PII encrypted columns in the file.");
								resource = new InputStreamResource(
										new BufferedInputStream(s3Object.getObjectContent()));
							}
						} else {
							log.info("There are no PII encrypted columns in the file.");
							resource = new InputStreamResource(new BufferedInputStream(s3Object.getObjectContent()));
						}

						return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
								.header(HttpHeaders.CONTENT_DISPOSITION,
										"attachment; filename=\"" + csvFileTmp.getName() + "\"")
								.body(resource);
					}
				} else {
					log.info("File with id: {} not found.", fileId);
					response = createSuccessMessage(400, "Requested file not found: " + fileId,
							"Requested file not found: " + fileId);
				}
			} else {
				log.info("Folder with id: {} not found.", folderId);
				response = createSuccessMessage(400, "Requested folder not found: " + folderId,
						"Requested folder not found: " + folderId);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			log.error("Error : {}", e.getMessage());
			response = createSuccessMessage(400, "Failed to download the file from S3 location.",
					"Failed to download the file from S3 location.");
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
				}
			}
		}

		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Download decrypted Engineered file.", response = Json.class)
	@GetMapping("/downloadengfile/{projectId}/{userId}/{engFlowId}/{fileType}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Downloaded file successfully"),
			@ApiResponse(code = 400, message = "Folder or File not found."), })
	public ResponseEntity<Object> downloadDecryptedFile(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "engFlowId") final int engFlowId,
			@PathVariable(value = "fileType") final String fileType) throws Exception{
		Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		BufferedWriter bw = null;
		File csvFileTmp = null;
		S3Object s3Object = null;
		try {
			Optional<EngFlow> engFlowOp = engFlowService.getFlowsForProjectAndId(projectId, engFlowId);
			if (engFlowOp.isPresent()) {
				EngFlow engFlow = engFlowOp.get();
				// Now fetch the Parent Engineering flow instance..

				Optional<EngFlow> parentEngFlowOp = engFlowService.getFlowsForProjectAndId(projectId,
						engFlow.getParentEngFlowId());
				if (parentEngFlowOp.isPresent()) {
					EngFlow parentEngFlow = parentEngFlowOp.get();
					String objectKey = String.format("%s/%s", parentEngFlow.getEngFlowName(), engFlow.getEngFlowName());
					String bucketName = project.getBucketName() + "-" + fileType;
					log.info("Object Key to searched in the bucket: {} is: {}", bucketName, objectKey);
					s3Object = awsAdapter.fetchObject(bucketName, objectKey);
					log.info("Object found in S3 and its key is: {}", s3Object.getKey());

					if (s3Object != null) {
						// Check if the file has encrypted columns or not.. If so, decrypt it and then
						// download..

						String tmpdir = System.getProperty("java.io.tmpdir");
						String path = s3Object.getKey();
						String tmpCsvFileNameWithoutExt = path.substring(path.lastIndexOf("/") + 1,
								path.lastIndexOf("."));
						String tmpCsvFileName = String.format("%s/%s/%s/tmp/%s.csv", tmpdir,
								parentEngFlow.getEngFlowName(), engFlow.getEngFlowName(), tmpCsvFileNameWithoutExt);
						csvFileTmp = new File(tmpCsvFileName);
						if (!csvFileTmp.getParentFile().exists()) {
							csvFileTmp.getParentFile().mkdirs();
						}

						log.info("CSV File to be decrypted before download is: {}", csvFileTmp.getAbsolutePath());

						String piiCols = "";

						EngFlowMetaDataAwsRef metadataAwsRef = engFlowMetaDataService.getByFlowId(engFlowId);
						String engMetadata = metadataAwsRef.getEngFlowMetaData();
						ObjectMapper mapper = new ObjectMapper();
						Resource resource = null;

						if (engMetadata != null) {
							JsonNode metadataNodes = mapper.readValue(engMetadata, JsonNode.class);
							Iterator<JsonNode> nodesIter = metadataNodes.iterator();
							List<String> colNames = new ArrayList<>();
							while (nodesIter.hasNext()) {
								colNames.add(nodesIter.next().get("column_name").asText());
							}

							log.info("Column Names are: {}", colNames.toString());

							if (!colNames.isEmpty()) {
								String colStr = colNames.toString();
								piiCols = colStr.substring(1, colStr.length() - 1);

								log.info("Column Names after splitting is: {}", piiCols);
								CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
								CsvMapper csvMapper = new CsvMapper();
								List<Object> readAllLines = csvMapper.readerFor(Map.class).with(csvSchema)
										.readValues(
												new BufferedReader(new InputStreamReader(s3Object.getObjectContent())))
										.readAll();
								//controllerHelper.decryptColumnsOfCsvFile(readAllLines, piiCols);  Commented bug:4830

								CsvSchema.Builder schemaBuilder = CsvSchema.builder();
								CsvSchema schema = null;
								if (readAllLines != null && !readAllLines.isEmpty()) {
									Object obj = readAllLines.get(0);
									Map<String, String> firstItem = (Map<String, String>) obj;
									for (String col : firstItem.keySet()) {
										schemaBuilder.addColumn(col);
									}
									schema = schemaBuilder.build().withLineSeparator("\r").withHeader();
									bw = new BufferedWriter(new FileWriter(csvFileTmp));
									csvMapper.writer(schema).writeValues(bw).writeAll(readAllLines);

									bw.flush();
								}

								Path filePath = Paths.get(csvFileTmp.getAbsolutePath()).toAbsolutePath().normalize();
								resource = new UrlResource(filePath.toUri());
							} else {
								log.info("There are no PII encrypted columns in the file.");
								resource = new InputStreamResource(
										new BufferedInputStream(s3Object.getObjectContent()));
							}
						} else {
							log.info("There are no PII encrypted columns in the file.");
							resource = new InputStreamResource(new BufferedInputStream(s3Object.getObjectContent()));
						}

						return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
								.header(HttpHeaders.CONTENT_DISPOSITION,
										"attachment; filename=\"" + csvFileTmp.getName() + "\"")
								.body(resource);
					}
				} else {
					log.info("Parent Engineering flow with id: {} not found.", engFlow.getParentEngFlowId());
					response = createSuccessMessage(400,
							"Parent Engineering flow not found: " + engFlow.getParentEngFlowId(),
							"Parent Engineering flow not found: " + engFlow.getParentEngFlowId());
				}
			} else {
				log.info("Engineering flow with id: {} not found.", engFlowId);
				response = createSuccessMessage(400, "Engineering flow not found: " + engFlowId,
						"Engineering flow not found: " + engFlowId);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			log.error("Error : {}", e.getMessage());
			response = createSuccessMessage(400, "Failed to download the file from S3 location.",
					"Failed to download the file from S3 location.");
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
				}
			}

			if (s3Object != null) {
				try {
					s3Object.close();
				} catch (IOException e) {
				}
			}
		}

		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Get file data to apply Standardize rule", response = Json.class)
	@GetMapping("/{projectId}/{folderId}/{fileId}/{columnName}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "join operation completed"),
			@ApiResponse(code = 400, message = "Folder or File not found."), })
	public ResponseEntity<Object> getColumnStatistics(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "folderId") final int folderId, @PathVariable(value = "fileId") final int fileId,
			@PathVariable(value = "columnName") final String columnName) throws Exception {
		Map<String, Object> response = new HashMap<>();
		dbUtil.changeSchema("public");
		//QsUserV2 user = validatorUtils.checkUser(userId);
		Projects project = validatorUtils.checkProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		try {
			QsFolders folder = folderService.getFolder(folderId);
			if (folder != null) {
				Optional<QsFiles> fileOp = fileService.getFileById(fileId);
				QsFiles file = null;
				if (fileOp.isPresent()) {
					file = fileOp.get();
					// Get the Partition Name for this file..
					Optional<QsPartition> partitionOp = partitionService.getPartitionByFileId(folderId, fileId);
					if (partitionOp.isPresent()) {
						QsPartition partition = partitionOp.get();

						ArrayNode columnStats = awsAdapter.getRawFileSingleColumnAnalytics(project,
								folder.getFolderName(), columnName, partition.getPartitionName());
						if (columnStats != null) {
							log.info("Column statistics successfully returned to the UI.");
							response = createSuccessMessage(200, "Successfully returned column statistics information.",
									columnStats);
						} else {
							log.info("Column statistics: {}", columnStats);
							response = createSuccessMessage(400, "Failed to fetch column statistics", null);
						}
					}
				} else {
					log.info("File with id: {} not found.", fileId);
					response = createSuccessMessage(400, "Requested file not found: " + fileId,
							"Requested file not found: " + fileId);
				}
			} else {
				log.info("Folder with id: {} not found.", folderId);
				response = createSuccessMessage(400, "Requested folder not found: " + folderId,
						"Requested folder not found: " + folderId);
			}
		} catch (final Exception e) {
			log.error("Error : {}", e.getMessage());
			response = createSuccessMessage(400,
					"Failed to fetch column statistics. Please check the column '" + columnName
							+ "' name provided as input.",
					"Failed to fetch column statistics. Please check the column '" + columnName
							+ "' name provided as input.");
		}
		return ResponseEntity.ok().body(response);
	}

	@SuppressWarnings("unused")
	private String getObjectKey(String bucketName, String folderName, int runJobId) {
		StringBuilder sb = new StringBuilder();
		sb.append("s3://");
		sb.append(bucketName);
		sb.append("/");
		sb.append(folderName);
		sb.append("/");
		sb.append(runJobId);

		return sb.toString();
	}

	private HashMap<String, Object> createSuccessMessage(final int code, final String message, Object result) {
		final HashMap<String, Object> success = new HashMap<>();
		success.put("code", code);
		success.put("result", result);
		success.put("message", message);
		return success;
	}

	private String getFileObjectKey(final int folderId, final int fileId) throws SQLException, MalformedURLException {

		Optional<QsPartition> partitionOp = partitionService.getPartitionByFileId(folderId, fileId);
		QsPartition partition = null;
		if (partitionOp.isPresent()) {
			partition = partitionOp.get();
		}

		String file1S3Location = partition.getS3Location();
		String file1ObjectKey = null;
		if (file1S3Location != null) {
			URL url = new URL(file1S3Location);
			file1ObjectKey = url.getPath();
			file1ObjectKey = file1ObjectKey.substring(1); // remove the trailing '/' character
			log.info("File S3 location: {}", file1S3Location);
			log.info("File Object key: {}", file1ObjectKey);
		}

		return file1ObjectKey;
	}

	private List<String> getColumnsList(String fileMetadata, String fileName, String folderName) throws Exception {
		if (fileMetadata == null || fileMetadata.isEmpty()) {
			return Collections.emptyList();
		}

		ObjectMapper mapper = new ObjectMapper();
		ArrayNode nodes = mapper.readValue(fileMetadata, ArrayNode.class);
		if (nodes != null) {
			List<String> cols = new ArrayList<>();
			Iterator<JsonNode> iter = nodes.iterator();
			while (iter.hasNext()) {
				cols.add(iter.next().get("column_name").asText() + " - " + fileName + " - " + folderName);
			}

			return cols;
		}

		return Collections.emptyList();
	}
}
