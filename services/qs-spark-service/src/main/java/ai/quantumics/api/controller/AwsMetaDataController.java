/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.CleansingParam;
import ai.quantumics.api.model.EngFlow;
import ai.quantumics.api.model.EngFlowMetaDataAwsRef;
import ai.quantumics.api.model.FileMetaDataAwsRef;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFiles;
import ai.quantumics.api.model.QsFolders;
import ai.quantumics.api.model.RedashViewInfo;
import ai.quantumics.api.model.RunJobStatus;
import ai.quantumics.api.req.Metadata;
import ai.quantumics.api.service.CleansingRuleParamService;
import ai.quantumics.api.service.EngFlowService;
import ai.quantumics.api.service.FileMetaDataAwsService;
import ai.quantumics.api.service.FileService;
import ai.quantumics.api.service.FolderService;
import ai.quantumics.api.service.RedashViewInfoService;
import ai.quantumics.api.service.RunJobService;
import ai.quantumics.api.util.MetadataHelper;
import ai.quantumics.api.util.UniqueResult;
import ai.quantumics.api.vo.AllFilesRedashResponse;
import ai.quantumics.api.vo.CleanseFilesRedashResponse;
import ai.quantumics.api.vo.CleanseFilesResponse;
import ai.quantumics.api.vo.CleanseFolderResponse;
import ai.quantumics.api.vo.CleanseHistoryResponse;
import ai.quantumics.api.vo.EngFileInfo;
import ai.quantumics.api.vo.EngineeredFilesRedashResponse;
import ai.quantumics.api.vo.RawFilesResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@RestController
@Api(value = "QuantumSpark Service API ")
public class AwsMetaDataController {
	private static final Gson gson = new Gson();
	private final ControllerHelper helper;
	private final FileService fileService;
	private final FolderService folderService;
	private final RunJobService runJobService;
	private final FileMetaDataAwsService fmAwsRef;
	private final EngFlowService engFlowService;
	private final RedashViewInfoService redashViewInfoService;
	private final MetadataHelper metadataHelper;
	private final CleansingRuleParamService cleansingRuleParamService;

	public AwsMetaDataController(ControllerHelper helperCi, FileService fileServiceCi, FolderService folderServiceCi,
			RunJobService runJobServiceCi, FileMetaDataAwsService fmAwsRefCi, EngFlowService engFlowServiceCi,
			RedashViewInfoService redashViewInfoServiceCi, MetadataHelper metadataHelperCi,
			CleansingRuleParamService cleansingRuleParamServiceCi) {
		helper = helperCi;
		fileService = fileServiceCi;
		folderService = folderServiceCi;
		runJobService = runJobServiceCi;
		fmAwsRef = fmAwsRefCi;
		engFlowService = engFlowServiceCi;
		redashViewInfoService = redashViewInfoServiceCi;
		metadataHelper = metadataHelperCi;
		cleansingRuleParamService = cleansingRuleParamServiceCi;
	}

	@ApiOperation(value = "Files Details for Redash", response = Json.class)
	@GetMapping("/api/v1/all/files/{projectId}/{userId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "List All QsFiles for Project ID & Folder ID") })
	public ResponseEntity<Object> getAllFiles(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId) {
		Map<String, Object> response;
		try {
			Projects projects = helper.getProjects(projectId);
			List<QsFolders> folders = folderService.getFoldersInProjectForUser(projectId, userId);
			Instant start = Instant.now();
			List<RawFilesResponse> rawFilesResponse = getFilesDetails(projects, folders, true);

			// Get the CleansedFiles details...
			List<CleanseFilesRedashResponse> cleansedFilesRedashRes = getCleansedFilesInfo(projects, folders, true);

			// Get the Processed files details... TODO: This func is yet to be implemented..
			List<EngineeredFilesRedashResponse> engFilesRedashRes = getCompletedEngFlowsInProject(projects, userId,
					true);

			// Finally prepare the Redash response instance for Raw, Cleansed and Engineered
			// Files for a
			// given Project and Folder Id's..

			AllFilesRedashResponse allFilesRedashRes = new AllFilesRedashResponse();
			allFilesRedashRes.setProjectId(projects.getProjectId());
			allFilesRedashRes.setRawFilesResponse(rawFilesResponse);
			allFilesRedashRes.setCleansedFilesResponse(cleansedFilesRedashRes);
			allFilesRedashRes.setEngineeredFilesResponse(engFilesRedashRes);

			response = helper.createSuccessMessage(200, "", allFilesRedashRes);
			Instant end = Instant.now();
			log.info("Time taken to calculate files data {}", Duration.between(start, end));
		} catch (Exception exception) {
			response = helper.failureMessage(500, "Error " + exception.getMessage());
			log.error("Error - {}", exception.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Files Details for Redash", response = Json.class)
	@GetMapping("/api/v1/redashinfo/files/{projectId}/{userId}/{type}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "List All QsFiles for Project ID & Folder ID") })
	public ResponseEntity<Object> getAllFilesForRedash(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "type") final String type) {
		Map<String, Object> response;
		try {
			Projects projects = helper.getProjects(projectId);

			if (QsConstants.REDASH_FILE_TYPE_PGSQL.equals(type)) {
				// helper.swtichToPublicSchema();
				List<RedashViewInfo> redashViews = redashViewInfoService.getViewsForSchema(projects.getDbSchemaName());
				if (redashViews != null && !redashViews.isEmpty()) {
					List<String> views = new ArrayList<>();
					redashViews.stream().forEach((redashView) -> {
						views.add(redashView.getSchemaName() + "." + redashView.getViewName());
					});
					response = helper.createSuccessMessage(200, "Successfully returned Redash Views Information.",
							views);
				} else {
					response = helper.createSuccessMessage(200,
							"There are no views present for the schema: " + projects.getDbSchemaName(),
							Collections.emptyList());
				}

				return ResponseEntity.ok().body(response);
			}

			// Below lines of code is applicable for type "aws".
			List<QsFolders> folders = folderService.getFoldersInProjectForUser(projectId, userId);
			Instant start = Instant.now();
			List<RawFilesResponse> rawFilesResponse = getFilesDetails(projects, folders, true);

			// Get the CleansedFiles details...
			List<CleanseFilesRedashResponse> cleansedFilesRedashRes = getCleansedFilesInfo(projects, folders, true);

			// Get the Processed files details... TODO: This func is yet to be implemented..
			List<EngineeredFilesRedashResponse> engFilesRedashRes = getCompletedEngFlowsInProject(projects, userId,
					true);

			// Finally prepare the Redash response instance for Raw, Cleansed and Engineered
			// Files for a
			// given Project and Folder Id's..

			AllFilesRedashResponse allFilesRedashRes = new AllFilesRedashResponse();
			allFilesRedashRes.setProjectId(projects.getProjectId());
			allFilesRedashRes.setRawFilesResponse(rawFilesResponse);
			allFilesRedashRes.setCleansedFilesResponse(cleansedFilesRedashRes);
			allFilesRedashRes.setEngineeredFilesResponse(engFilesRedashRes);

			log.info("All files (Raw, Cleansed and Engineered) JSON response returned to UI");

			response = helper.createSuccessMessage(200, "Successfully returned Redash Files Information.",
					allFilesRedashRes);
			Instant end = Instant.now();
			log.info("Time taken to calculate files data {}", Duration.between(start, end));
		} catch (Exception exception) {
			response = helper.failureMessage(500, "Error " + exception.getMessage());
			log.error("Error - {}", exception.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "History of cleanse runs for a file", response = Json.class)
	@GetMapping("/api/v1/history/{projectId}/{userId}/{folderId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "History of cleanse runs for a file") })
	public ResponseEntity<Object> getHistory(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "folderId") final int folderId) {
		Map<String, Object> response = new HashMap<>();
		try {
			Projects projects = helper.getProjects(projectId, userId);
			if (projects == null) {
				response.put("code", HttpStatus.BAD_REQUEST);
				response.put("message",
						"Requested project with Id: " + projectId + " for User with Id: " + userId + " not found.");

				return ResponseEntity.ok().body(response);
			}

			Instant start = Instant.now();
			List<RunJobStatus> cleanseFilesByProjectId = runJobService.getAllFilesHistory(projects.getProjectId(),
					folderId);
			log.info("Found {} files history for the projectId - {} and folderId - {} ", cleanseFilesByProjectId.size(),
					projectId, folderId);
			HashMap<Integer, String> fileMapInfo;

			fileMapInfo = cleanseFilesByProjectId.stream()
					.map(runJobStatus -> fileService.getFileById(runJobStatus.getFileId())).filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toMap(QsFiles::getFileId, QsFiles::getFileName, (a, b) -> b, HashMap::new));

			log.info("No. of active files :: {}", fileMapInfo.size());

			List<CleanseHistoryResponse> cleanseHistory = new ArrayList<>();
			final ObjectMapper mapper = new ObjectMapper();
			cleanseFilesByProjectId.forEach(runJobStatus -> {
				if (fileMapInfo.containsKey(runJobStatus.getFileId())) {
					CleanseHistoryResponse responseObject = getCleanseHistoryRespose(fileMapInfo, mapper, runJobStatus);

					cleanseHistory.add(responseObject);
				}
			});

			if (cleanseHistory != null && !cleanseHistory.isEmpty()) {
				Collections.sort(cleanseHistory, new Comparator<CleanseHistoryResponse>() {
					@Override
					public int compare(CleanseHistoryResponse o1, CleanseHistoryResponse o2) {
						Integer i1 = Integer.valueOf(o1.getRunJobId());
						Integer i2 = Integer.valueOf(o2.getRunJobId());

						return i2.compareTo(i1);
					}
				});
			}

			log.info("History Final :: {}", cleanseHistory.size());
			Instant end = Instant.now();
			log.info("Total Time taken :: {}", Duration.between(start, end));
			response = helper.createSuccessMessage(200, "cleansed Files", cleanseHistory);
		} catch (Exception exception) {
			response = helper.failureMessage(500, "Error " + exception.getMessage());
			log.error("Error - {}", exception.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}

	/**
	 * @param fileMapInfo
	 * @param mapper
	 * @param runJobStatus
	 * @return
	 */
	private CleanseHistoryResponse getCleanseHistoryRespose(HashMap<Integer, String> fileMapInfo,
			final ObjectMapper mapper, RunJobStatus runJobStatus) {
		CleanseHistoryResponse responseObject = new CleanseHistoryResponse();
		responseObject.setFileId(runJobStatus.getFileId());
		responseObject.setRunJobId(runJobStatus.getRunJobId());
		responseObject.setProjectId(runJobStatus.getProjectId());
		responseObject.setStatus(runJobStatus.getStatus());
		responseObject.setCreatedDate(runJobStatus.getCreatedDate());
		responseObject.setFolderId(runJobStatus.getFolderId());
		responseObject.setFileName(fileMapInfo.get(runJobStatus.getFileId()));

		if (runJobStatus.getBatchJobLog() != null) {
			String batchLog = runJobStatus.getBatchJobLog();
			JsonNode node;
			try {
				node = mapper.readValue(batchLog, JsonNode.class);
				responseObject.setBatchJobLog(node.toPrettyString());
			} catch (JsonProcessingException jsonProcessingException) {
				log.error("Error while setting batch log :" + jsonProcessingException.getMessage());
			}
		}
		return responseObject;
	}

	@ApiOperation(value = "folders for cleanse", response = Json.class)
	@GetMapping("/api/v1/cleanse/{projectId}/{userId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "List Files for the project cleansed") })
	public ResponseEntity<Object> getCleanseFoldersOnly(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId) {
		Map<String, Object> response;
		try {
			helper.getProjects(projectId);
			List<CleansingParam> cleansingParamFolders = getCleanseParamFolders();
			List<CleanseFolderResponse> cleanseResponse = new ArrayList<>();
			getRefinedFolderNames(cleanseResponse, cleansingParamFolders, projectId, userId);
			log.info("Total folders: {}", cleanseResponse.size());
			response = helper.createSuccessMessage(200, "cleansed Files", cleanseResponse);
		} catch (Exception exception) {
			response = helper.failureMessage(500, "Error " + exception.getMessage());
			log.error("Error - {}", exception.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}
	
	@ApiOperation(value = "Get All Active cleanse jobs", response = Json.class, notes = "Get All Active cleanse jobs")
	@ApiResponses(value = { @ApiResponse(code = 200, message = " cleanse jobs Found!"),
			@ApiResponse(code = 404, message = " cleanse jobs Not Found!") })
	@GetMapping(value = "/api/v1/cleanseJobsActive/{userId}/{projectId}")
	public ResponseEntity<Object> getAllEngFlows(@PathVariable(value = "userId") final int userId,
			@PathVariable(value = "projectId") final int projectId) {
		Map<String, Object> response;
		try {
			Projects projects = helper.getProjects(projectId);
			helper.getProjectAndChangeSchema(projectId);
			List<QsFolders> folders = folderService.getFoldersInProjectForUser(projectId, userId);
			List<CleanseFilesRedashResponse> cleansedFilesRedashRes = getCleansedFilesInfo(projects, folders, false);
			response = helper.createSuccessMessage(200, "cleans jobs Objects", cleansedFilesRedashRes);
		} catch (Exception exception) {
			response = helper.failureMessage(500, exception.getMessage());
			log.error("Exception reading cleanseJobsActive {}", exception.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@ApiOperation(value = "folders for cleanse", response = Json.class)
	@GetMapping("/api/v1/cleanse/folders/{projectId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "List Folders for the cleansed Files") })
	public ResponseEntity<Object> getCleanseFoldersInfo(@PathVariable(value = "projectId") final int projectId) {
		Map<String, Object> response;
		try {
			helper.getProjects(projectId);
			List<RunJobStatus> cleanseFolders = getRunJobFolders(projectId);
			List<QsFolders> folders = new ArrayList<>();
			cleanseFolders.stream().forEach((runJobStatus) -> {
				QsFolders folder = folderService.getFolder(runJobStatus.getFolderId());
				if (folder != null) {
					folders.add(folder);
				}
			});
			log.info("No. folders Found {} for the project {}", folders.size(), projectId);
			response = helper.createSuccessMessage(200, "cleansed Files", folders);
		} catch (Exception exception) {
			response = helper.failureMessage(500, "Error " + exception.getMessage());
			log.error("Error - {}", exception.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}
	
	

	private List<RunJobStatus> getRunJobFolders(int projectId) throws SQLException {
		List<RunJobStatus> cleanseFilesByProjectId = runJobService.getCleanseFilesByProjectId(projectId);
		cleanseFilesByProjectId = cleanseFilesByProjectId.stream()
				.filter(UniqueResult.distinctByKey(RunJobStatus::getFolderId))
				.sorted(Comparator.comparing(RunJobStatus::getCreatedDate).reversed()).collect(Collectors.toList());
		return cleanseFilesByProjectId;
	}

	private List<CleansingParam> getCleanseParamFolders() throws SQLException {
		List<CleansingParam> cleansingParams = cleansingRuleParamService.getRulesOrderByModifiedDateDesc();
		cleansingParams = cleansingParams.stream().filter(UniqueResult.distinctByKey(CleansingParam::getFolderId))
				.collect(Collectors.toList());
		return cleansingParams;
	}

	@ApiOperation(value = "Files for cleanse", response = Json.class)
	@GetMapping("/api/v1/cleanse/files/{projectId}/{folderId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "List Files for the project cleansed") })
	public ResponseEntity<Object> getCleanseFilesInfo(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "folderId") final int folderId) {
		Map<String, Object> response;
		try {
			helper.getProjects(projectId);
			List<RunJobStatus> cleanseFiles = getRunJobFolders(projectId);
			cleanseFiles = cleanseFiles.stream().filter(UniqueResult.distinctByKey(RunJobStatus::getFileId))
					.filter(runJobStatus -> runJobStatus.getFolderId() == folderId).collect(Collectors.toList());
			log.info("size of the folders unique {}", cleanseFiles.size());
			List<FileMetaDataAwsRef> cleansedFilesInfo = new ArrayList<>();
			cleanseFiles.forEach(runJobStatus -> {
				cleansedFilesInfo.addAll(fmAwsRef.getDistinctFiles(runJobStatus.getFileId()).stream()
						.filter(UniqueResult.distinctByKey(FileMetaDataAwsRef::getFileId))
						.collect(Collectors.toList()));
			});
			List<CleanseFilesResponse> cleansedFilesOnly = prepareResponseObject(cleansedFilesInfo);

			log.info("No.. Cleansed Files found {} for folder {}", cleansedFilesOnly.size(), folderId);
			response = helper.createSuccessMessage(200, "cleansed Files", cleansedFilesOnly);
		} catch (Exception exception) {
			response = helper.failureMessage(500, "Error " + exception.getMessage());
			log.error("Error - {}", exception.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}

	@ApiOperation(value = "Files Details for a Project", response = Json.class)
	@GetMapping("/api/v1/getprojectfilesinfo/{projectId}/{userId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "List All QsFiles for Project Id.") })
	public ResponseEntity<Object> getProjectFilesInfo(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId) {

		Map<String, Object> response;

		try {
			Projects projects = helper.getProjects(projectId);
			List<QsFolders> folders = folderService.getFoldersInProjectForUser(projectId, userId);
			Instant start = Instant.now();

			List<RawFilesResponse> rawFilesResponse = getFilesDetails(projects, folders, false);

			// Get the CleansedFiles details...
			List<CleanseFilesRedashResponse> cleansedFilesRedashRes = getCleansedFilesInfo(projects, folders, false);

			// Get the Processed files details...
			List<EngineeredFilesRedashResponse> engFilesRedashRes = getCompletedEngFlowsInProject(projects, userId,
					false);

			// Finally prepare the Redash response instance for Raw, Cleansed and Engineered
			// Files for a
			// given Project and Folder Id's..

			AllFilesRedashResponse allFilesRedashRes = new AllFilesRedashResponse();
			allFilesRedashRes.setProjectId(projects.getProjectId());
			allFilesRedashRes.setRawFilesResponse(rawFilesResponse);
			allFilesRedashRes.setCleansedFilesResponse(cleansedFilesRedashRes);
			allFilesRedashRes.setEngineeredFilesResponse(engFilesRedashRes);

			log.info("All files (Raw, Cleansed and Engineered) JSON response returned to UI");
			response = helper.createSuccessMessage(200, "", allFilesRedashRes);
			Instant end = Instant.now();
			log.info("Time taken to calculate files data {}", Duration.between(start, end));
		} catch (Exception exception) {
			response = helper.failureMessage(500, "Error " + exception.getMessage());
			log.error("Error - {}", exception.getMessage());
		}
		return ResponseEntity.ok().body(response);
	}

	private void getRefinedFolderNames(List<CleanseFolderResponse> cleanseResponse,
			List<CleansingParam> finalListFolders, int projectId, int userId) {
		for (CleansingParam cleansingParam : finalListFolders) {
			QsFolders folder1 = folderService.getActiveFolder(cleansingParam.getFolderId(), projectId, userId);
			if (folder1 != null) {
				CleanseFolderResponse responseObj = new CleanseFolderResponse();
				responseObj.setFolderId(folder1.getFolderId());
				responseObj.setUserId(folder1.getUserId());
				responseObj.setProjectId(folder1.getProjectId());
				responseObj.setFolderName(folder1.getFolderName());
				responseObj.setFolderDisplayName((folder1.getFolderDisplayName() == null) ? folder1.getFolderName()
						: folder1.getFolderDisplayName());

				try {
					String createdByCurr = folder1.getCreatedBy();
					int id = Integer.parseInt(createdByCurr);
					String createdBy = helper.getUserName(id);

					responseObj.setCreatedBy(createdBy);
				} catch (Exception e) {
					// Exception can occur for the folders created initially, where, the created by
					// field is the actual string and not User Id (Integer)
					responseObj.setCreatedBy(folder1.getCreatedBy());
				}

				responseObj.setCreateDate(folder1.getCreatedDate());
				responseObj.setModifiedDate(folder1.getModifiedDate());
				
				helper.getProjectAndChangeSchema(projectId);
				try {
					responseObj.setFilesCount(fmAwsRef.getAllByFileId(cleansingParam.getFileId()).size());
				} catch (Exception e) {
					responseObj.setFilesCount(0);
				}
				List<FileMetaDataAwsRef> distinctFileDesc = fmAwsRef.getDistinctFileDesc(cleansingParam.getFileId());

				if (distinctFileDesc != null && !distinctFileDesc.isEmpty()) {
					Optional<QsFiles> latestFileInFolder = fileService.getFileById(distinctFileDesc.get(0).getFileId());
					if (latestFileInFolder.isPresent()) {
						responseObj.setLatestFileName(latestFileInFolder.get().getFileName());
					} else {
						responseObj.setLatestFileName("");
					}
				} else {
					responseObj.setLatestFileName("");
				}

				cleanseResponse.add(responseObj);
			}
		}
	}

	private List<RawFilesResponse> getFilesDetails(Projects projects, List<QsFolders> folders, boolean redashReq)
			throws SQLException {
		int projectId = projects.getProjectId();
		List<RawFilesResponse> folderResponse = new ArrayList<>();
		for (QsFolders folder : folders) {
			RawFilesResponse filesResponse = new RawFilesResponse();
			filesResponse.setCategory("raw");
			filesResponse.setType("folder");
			filesResponse.setFolderId(folder.getFolderId());
			filesResponse.setFolderName(
					(folder.getFolderDisplayName() == null) ? folder.getFolderName() : folder.getFolderDisplayName());
			filesResponse.setExternal(folder.isExternal());
			List<QsFiles> files = fileService.getFiles(projectId, folder.getFolderId());
			List<EngFileInfo> fileInfoList = new ArrayList<>();
			for (QsFiles file : files) {
				EngFileInfo engFileInfo = new EngFileInfo();
				if (redashReq) {
					engFileInfo.setFileName(file.getFileName());
					engFileInfo.setTableName(projects.getRawDb() + "." + folder.getFolderName());
				} else {
					engFileInfo.setFileId(file.getFileId());
					engFileInfo.setFileName(file.getFileName());
					engFileInfo.setCategory("raw");
					engFileInfo.setFolderId(file.getFolderId());
					engFileInfo.setMetadata(gson.fromJson(file.getQsMetaData(), List.class));
				}

				fileInfoList.add(engFileInfo);
			}

			filesResponse.setFiles(fileInfoList);
			folderResponse.add(filesResponse);
		}
		return folderResponse;
	}

	private List<CleanseFilesResponse> prepareResponseObject(List<FileMetaDataAwsRef> cleansedFilesInfo) {
		List<CleanseFilesResponse> cleansedFilesOnly = new ArrayList<>();
		for (FileMetaDataAwsRef fileMetaDataAwsRef : cleansedFilesInfo) {
			CleanseFilesResponse fileObject = new CleanseFilesResponse();
			Optional<QsFiles> fileById = fileService.getFileById(fileMetaDataAwsRef.getFileId());
			if (fileById.isPresent()) {
				QsFiles qsFiles = fileById.get();
				fileObject.setFileName(qsFiles.getFileName());
				fileObject.setFileId(fileMetaDataAwsRef.getFileId());
				fileObject.setFileType(fileMetaDataAwsRef.getFileType());
				fileObject.setAthenaTable(fileMetaDataAwsRef.getAthenaTable());
				fileObject.setTablePartition(fileMetaDataAwsRef.getTablePartition());
				fileObject.setCreatedDate(fileMetaDataAwsRef.getCreatedDate());
				fileObject.setCreatedBy(fileMetaDataAwsRef.getCreatedBy());
				fileObject.setFileMetaData(fileMetaDataAwsRef.getFileMetaData());
			}
			cleansedFilesOnly.add(fileObject);
		}
		return cleansedFilesOnly;
	}

	private List<RunJobStatus> getRunJobFolders(int projectId, int folderId) {
		List<RunJobStatus> cleanseFilesByProjectId = runJobService.getAllFilesHistory(projectId, folderId);
		cleanseFilesByProjectId = cleanseFilesByProjectId.stream()
				.filter(UniqueResult.distinctByKey(RunJobStatus::getFileId)).collect(Collectors.toList());
		return cleanseFilesByProjectId;
	}

	private List<CleanseFilesRedashResponse> getCleansedFilesInfo(Projects project, List<QsFolders> folders,
			boolean redashReq) throws Exception {

		log.info("Request for fetching the Cleansed files info with Project Id: {} and Folders: {} is received.",
				project.getProjectId());

		List<FileMetaDataAwsRef> cleansedFilesInfo = new ArrayList<>();
		List<CleanseFilesRedashResponse> cleansedFilesRedashResList = new ArrayList<>();
		List<RunJobStatus> cleanseFiles = new ArrayList<>();

		for (QsFolders folder : folders) {
			cleanseFiles.clear();
			cleanseFiles = getRunJobFolders(project.getProjectId(), folder.getFolderId());

			// First clear this list as it is reused..
			cleansedFilesInfo.clear();

			cleanseFiles.stream().forEach(runJobStatus -> {
				cleansedFilesInfo.addAll(fmAwsRef.getDistinctFileDesc(runJobStatus.getFileId()));
			});

			CleanseFilesRedashResponse cleansedFilesRedashRes = new CleanseFilesRedashResponse();
			cleansedFilesRedashRes.setCategory("processed"); // Need to externalize using a String constant.
			cleansedFilesRedashRes.setType("folder");
			cleansedFilesRedashRes.setFolderId(folder.getFolderId());
			cleansedFilesRedashRes.setFolderName(
					(folder.getFolderDisplayName() == null) ? folder.getFolderName() : folder.getFolderDisplayName());
			cleansedFilesRedashRes.setExternal(false);

			List<EngFileInfo> files = new ArrayList<>();
			for (FileMetaDataAwsRef cleansedFileRef : cleansedFilesInfo) {
				Optional<QsFiles> fileOp = fileService.getFileById(cleansedFileRef.getFileId());
				if (fileOp.isPresent()) {
					EngFileInfo engFileInfo = new EngFileInfo();
					if (redashReq) {
						engFileInfo.setFileName(fileOp.get().getFileName());
						engFileInfo.setTableName(project.getProcessedDb() + "." + cleansedFileRef.getAthenaTable());
					} else {
						engFileInfo.setFileId(cleansedFileRef.getFileId());
						engFileInfo.setFileName(fileOp.get().getFileName());
						engFileInfo.setCategory("processed");

						Optional<QsFiles> fileOp2 = fileService.getFileById(cleansedFileRef.getFileId());
						QsFiles cleansedFile = null;
						if (fileOp2.isPresent()) {
							cleansedFile = fileOp2.get();
						}

						engFileInfo.setFolderId(cleansedFile.getFolderId());
						engFileInfo.setMetadata(gson.fromJson(cleansedFileRef.getFileMetaData(), List.class));
					}

					files.add(engFileInfo);
				}
			}

			cleansedFilesRedashRes.setFiles(files);
			cleansedFilesRedashResList.add(cleansedFilesRedashRes);
		}

		log.info("Number of Cleansed Files prepared for Redash is: {}",
				(cleansedFilesRedashResList != null) ? cleansedFilesRedashResList.size() : 0);

		return cleansedFilesRedashResList;
	}

	@SuppressWarnings("unused")
	private List<Metadata> getMetadataAsList(String metadataStr) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> values = mapper.readValue(metadataStr, Map.class);
		if (values != null && !values.isEmpty()) {
			return values.keySet().stream()
					.map(value -> (new Metadata(value, value.substring(value.lastIndexOf("_") + 1))))
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	private List<EngineeredFilesRedashResponse> getCompletedEngFlowsInProject(Projects project, int userId,
			boolean redashReq) throws Exception {

		log.info("Fetching the Engineered Files info--> ");

		List<EngineeredFilesRedashResponse> engFilesRedashResList = new ArrayList<>();
		List<EngFlow> activeEngFlows = null;
		// if(redashReq) {
		// activeEngFlows = engFlowService.getFlowNames(project.getProjectId());
		// } else {
		activeEngFlows = engFlowService.getFlowNames(project.getProjectId(), userId);
		// }

		if (activeEngFlows != null && !activeEngFlows.isEmpty()) {
			log.info("Active engineering flows are {}", activeEngFlows.size());

			EngineeredFilesRedashResponse engFilesRedashRes = new EngineeredFilesRedashResponse();
			engFilesRedashRes.setCategory(QsConstants.ENGINEERED);
			engFilesRedashRes.setType("ENG");
			engFilesRedashRes.setExternal(false);
			List<EngFileInfo> files = new ArrayList<>();

			for (EngFlow engFlow : activeEngFlows) {
				EngFlowMetaDataAwsRef completedFlow = metadataHelper
						.getLatestCompletedEngFlowsInfo(engFlow.getEngFlowName());
				if (completedFlow != null) {
					EngFileInfo engFileInfo = new EngFileInfo();
					engFileInfo.setFileId(engFlow.getEngFlowId());
					engFileInfo.setCategory("eng");

					if (!redashReq) {
						engFileInfo.setMetadata(gson.fromJson(completedFlow.getEngFlowMetaData(), List.class));
						engFileInfo.setTableName(completedFlow.getAthenaTable());
						engFileInfo.setFileName(engFlow.getEngFlowName());
					} else {
						engFileInfo.setTableName(
								(project.getEngDb() + "." + completedFlow.getAthenaTable()).toLowerCase());
						engFileInfo.setFileName((project.getEngDb() + "." + engFlow.getEngFlowName()).toLowerCase());
					}

					files.add(engFileInfo);
				}
			}

			if (!files.isEmpty()) {
				engFilesRedashRes.setFiles(files);
				engFilesRedashResList.add(engFilesRedashRes);
			}
		}

		log.info("Prepared the final list of Engineered flows and the count is: {}",
				(engFilesRedashResList != null) ? engFilesRedashResList.size() : 0);

		return engFilesRedashResList;
	}
}
