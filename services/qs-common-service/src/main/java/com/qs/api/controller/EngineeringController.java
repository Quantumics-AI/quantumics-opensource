package com.qs.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.qs.api.model.*;
import com.qs.api.request.*;
import com.qs.api.service.*;
import com.qs.api.util.JobRunState;
import com.qs.api.util.MetadataHelper;
import com.qs.api.util.QsConstants;
import com.qs.api.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import java.sql.SQLException;
import java.util.*;

import static com.qs.api.util.QsConstants.*;

@Slf4j
@RestController
@Api(value = "QuantumSpark Service API ")
@Component
public class EngineeringController {
  private final ControllerHelper helper;
  private final EngFlowService engFlowService;
  private final EngFlowEventService engFlowEventService;
  private final FolderService folderService;
  private final FileService fileService;
  private final MetadataHelper metadataHelper;
  private final EngFlowJobService engFlowJobService;
  private final RunJobService runJobService;
  private final FileMetaDataAwsService fileMetaDataAwsService;
  private final EngFlowMetaDataService engFlowMetadataService;
  private final UdfService udfService;

  private static final Gson gson = new Gson();

  public EngineeringController(ControllerHelper helperCi, EngFlowService engFlowService,
      EngFlowEventService engFlowEventService, FolderService folderService, FileService fileService,
      MetadataHelper metadataHelper, EngFlowJobService engFlowJobService,
      RunJobService runJobService, FileMetaDataAwsService fileMetaDataAwsService,
      EngFlowMetaDataService engFlowMetadataService, UdfService udfService) {
    this.helper = helperCi;
    this.engFlowService = engFlowService;
    this.engFlowEventService = engFlowEventService;
    this.folderService = folderService;
    this.fileService = fileService;
    this.metadataHelper = metadataHelper;
    this.engFlowJobService = engFlowJobService;
    this.runJobService = runJobService;
    this.fileMetaDataAwsService = fileMetaDataAwsService;
    this.engFlowMetadataService = engFlowMetadataService;
    this.udfService = udfService;
  }

  @ApiOperation(value = "Get graph contents", response = Json.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Graph rendered"),
      @ApiResponse(code = 404, message = "Not found!")})
  @GetMapping(value = "/api/v1/graph/{userId}/{projectId}/{engFlowId}")
  public ResponseEntity<Object> getXmlContent(@PathVariable(value = "userId") final int userId,
      @PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "engFlowId") final int engFlowId) {

    Map<String, Object> response = helper.failureMessage(404, "Empty - Zero records found!");
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/api/v1/ops/file")
  public ResponseEntity<Object> livyFileOperation(@RequestBody DataFrameRequest wsRequest) {
    log.info("File operation requested for: {}", wsRequest);
    HashMap<String, Object> eventResponse = new HashMap<>();
    try {
      
      // Check whether the request is coming for the Engineered File.
      helper.getProjectAndChangeSchema(wsRequest.getProjectId());
      if(ENG_OP.equals(wsRequest.getFileType())) {
        EngFileOperationRequest engFileRequest = new EngFileOperationRequest();
        engFileRequest.setProjectId(wsRequest.getProjectId());
        engFileRequest.setUserId(wsRequest.getUserId());
        engFileRequest.setFileId(wsRequest.getFileId());
        engFileRequest.setEngFlowId(wsRequest.getEngFlowId());
        engFileRequest.setEngFlowName(wsRequest.getEngFlowName());
        engFileRequest.setFileType(ENG_OP);
        engFileRequest.setEventId(0);
        
        eventResponse = livyEngOperation(engFileRequest);
      }else {
        if (wsRequest.getProjectId() > 0 && wsRequest.getFolderId() > 0
            && wsRequest.getFileId() > 0) {
          EngFlowEvent flowEvent = helper.getEngFlowEvent(wsRequest);
          wsRequest.setEventId(flowEvent.getEngFlowEventId());
          updateMetadata(wsRequest.getFileId(), flowEvent, wsRequest.getFileType());
          log.info("Updated EngFlowEvent instance :: {}", flowEvent);

          // Save the updated Event
          flowEvent = engFlowEventService.save(flowEvent);
          
          helper.updateFlowEventWithData(wsRequest);
          eventResponse = helper.createSuccessMessage(200, "Action saved!",
             flowEvent.getEngFlowEventId(), flowEvent.getFileMetaData());
        } else {
          eventResponse = helper.failureMessage(400, "BadRequest !");
          log.error("Attributes cant not be zero!");
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace();
      eventResponse =
          helper.failureMessage(500, "Internal Server Error !" + exception.getMessage());
      log.error("Exception - {}", exception.getMessage());
    }

    return ResponseEntity.status(HttpStatus.OK).body(eventResponse);
  }

  @PostMapping("/api/v1/ops/join")
  public ResponseEntity<Object> livyJoinOperation(@RequestBody JoinOperationRequest wsJoinRequest) {
    log.info("Join Operation requested for {}", wsJoinRequest.toString());
    HashMap<String, Object> joinResponse = new HashMap<>();
    try {
      Projects project = helper.getProjects(wsJoinRequest.getProjectId());
      EngFlowEvent joinAction = null;
      if (wsJoinRequest.getEventId() == 0) {
        joinAction = helper.getEventForJoinAction(wsJoinRequest);
      } else {

        Optional<EngFlowEvent> engFlowEventRecord =
            engFlowEventService.getFlowEvent(wsJoinRequest.getEventId());
        if (engFlowEventRecord.isPresent()) {
          joinAction = engFlowEventRecord.get();

          joinAction.setEngFlowConfig(gson.toJson(wsJoinRequest));
          joinAction.setEngFlowEventData(null);
          
          // First save the updated Request..
          joinAction = engFlowEventService.save(joinAction);
          wsJoinRequest.setEventId(wsJoinRequest.getEventId());
          wsJoinRequest.setUserId(project.getUserId());

          helper.updateFlowEventWithJoinResults(wsJoinRequest);
          log.info("Performing join operation :: for event {} and for flow {}",
              joinAction.getEngFlowEventId(), joinAction.getEngFlowId());
        }
      }

      joinResponse = helper.createSuccessMessage(200, "Join Action Event captured",
          joinAction.getEngFlowEventId());

    } catch (final Exception e) {
      log.error("Exception while performing join operation :: {}", e.getMessage());
      joinResponse = helper.failureMessage(500, "Internal Server error " + e.getMessage());
    }

    return ResponseEntity.status(HttpStatus.OK).body(joinResponse);
  }
  
  @PostMapping("/api/v1/ops/udf")
  public ResponseEntity<Object> udfOperation(@RequestBody final UdfOperationRequest wsUdfRequest) {
	  log.info("Udf Operation requested for {}", wsUdfRequest.toString());
	  HashMap<String, Object> joinResponse = new HashMap<>();
	  QsUdf udf = null;
	  Projects project = null;
	  try {
		   project = helper.getProjects(wsUdfRequest.getProjectId());
		  EngFlowEvent udfAction = null;
		  Map<String, Integer> eventIds = wsUdfRequest.getEventIds();
		  int eventId = eventIds.get("eventId");
		  if (eventId == 0) {
			  udfAction = helper.getEventForUdfAction(wsUdfRequest);
			  if(udfAction != null) {
				  helper.swtichToPublicSchema();
				  udf = udfService.getByUdfIdAndProjectIdAndUserId(wsUdfRequest.getFileId(), wsUdfRequest.getProjectId(), project.getUserId());
				  project = helper.getProjects(wsUdfRequest.getProjectId());
			  }
		  } else {
			  Optional<EngFlowEvent> engFlowEventRecord = engFlowEventService.getFlowEvent(eventId);
			  if (engFlowEventRecord.isPresent()) {
				  udfAction = engFlowEventRecord.get();

				  udfAction.setEngFlowConfig(gson.toJson(wsUdfRequest));
				  udfAction.setEngFlowEventData(null);
				  udfAction.setFileType(wsUdfRequest.getFileType());
				  udfAction.setFileId(wsUdfRequest.getFileId());
				  udfAction.setJoinOperations(wsUdfRequest.getUdfFunction());
				  udfAction.setProjectId(wsUdfRequest.getProjectId());
				  
				  // First save the updated Request..
				  udfAction = engFlowEventService.save(udfAction);
				  eventIds.put("eventId", eventId);
				  wsUdfRequest.setEventIds(eventIds);
				  wsUdfRequest.setUserId(project.getUserId());
				  
				  helper.updateFlowEventWithUdfResults(wsUdfRequest);
				  log.info("Performing udf operation :: for event {} and for flow {}", udfAction.getEngFlowEventId(), udfAction.getEngFlowId());
			  }
		  }
		  if(udf != null) {
			  joinResponse = helper.createSuccessMessage(200, "Udf Action Event captured", udfAction.getEngFlowEventId(), udf);
		  }else {
			  joinResponse = helper.createSuccessMessage(200, "Udf Action Event captured", udfAction.getEngFlowEventId());
		  }
		  
	  } catch (final Exception e) {
		  log.error("Exception while performing udf operation :: {}", e.getMessage());
		  joinResponse = helper.failureMessage(500, "Internal Server error " + e.getMessage());
	  }
	  return ResponseEntity.status(HttpStatus.OK).body(joinResponse);
  }
  
  
    
  @PostMapping("/api/v1/ops/runconfigauto")
  public ResponseEntity<Object> processConfigAutomation(
      @RequestBody EngFlowConfigRequest newEngFlowConfigRequest) {
    log.info("Run automation operation requested for {}", newEngFlowConfigRequest.toString());
    HashMap<String, Object> joinResponse = new HashMap<>();

    // Get the list of New Files selected in the Automation Screen
    List<DataFrameRequest> newFiles = newEngFlowConfigRequest.getSelectedFiles();
    Map<Integer, EngFlowEvent> newFileEvents = new HashMap<>();
    Map<Integer, Integer> processedEventsMap = new HashMap<>();
    int flowJobId = -1;
    try {

      helper.getProjectAndChangeSchema(newEngFlowConfigRequest.getProjectId());
      EngFlowConfigRequest origEngFlowConfigReq = null;
      EngFlow origEngFlow = null;
      // Get the Engineering Id from the EngFlowConfigRequest JSON and fetch the
      // Original Run Config JSON
      Optional<EngFlow> engFlowOpt = engFlowService.getFlowsForProjectAndId(
          newEngFlowConfigRequest.getProjectId(), newEngFlowConfigRequest.getEngineeringId());
      if (engFlowOpt.isPresent()) {
        origEngFlow = engFlowOpt.get();
        String origRunConfigStr = origEngFlow.getEngFlowConfig();
        
        ObjectMapper mapper = new ObjectMapper();
        // Convert it into EngFlowConfigRequest instance for easy processing later...
        origEngFlowConfigReq = mapper.readValue(origRunConfigStr, EngFlowConfigRequest.class);
        log.info("Original Engineering Run config JSON is: {} ", origEngFlowConfigReq);

        if(origEngFlowConfigReq != null) {
          EngFlow newEngFlow = new EngFlow();
          newEngFlow.setProjectId(newEngFlowConfigRequest.getProjectId());
          newEngFlow.setUserId(newEngFlowConfigRequest.getUserId());
          newEngFlow.setParentEngFlowId(newEngFlowConfigRequest.getEngineeringId());
          newEngFlow.setActive(true);
          newEngFlow.setCreatedDate(QsConstants.getCurrentUtcDate());
          newEngFlow.setCreatedBy(newEngFlowConfigRequest.getUserId() + ""); 
          newEngFlow.setEngFlowConfig(gson.toJson(origEngFlowConfigReq));
          newEngFlow.setEngFlowMetaData(origEngFlow.getEngFlowMetaData());
          EngFlow savedEngFlow  = engFlowService.save(newEngFlow);
          
          // Get the event ids for the new files selected in the Automation screen..
          if (newFiles != null && !newFiles.isEmpty()) {
            // Get the Event Ids for these files.
            log.info("Started creating events for the newly selected files...");
            for (DataFrameRequest newFile : newFiles) {
              EngFlowEvent fileEvent = null;
              String originalFileType = newFile.getFileType();
              int originalEventId = newFile.getEventId();
              
              if(QsConstants.ENG_OP.equals(newFile.getFileType())) {
                EngFileOperationRequest engRequest = new EngFileOperationRequest();
                engRequest.setProjectId(newFile.getProjectId());
                engRequest.setEngFlowId(savedEngFlow.getEngFlowId());
                engRequest.setUserId(newFile.getUserId());
                engRequest.setFileType(newFile.getFileType());
                engRequest.setEngFlowName(newFile.getEngFlowName());
                engRequest.setAutoConfigEventId(newFile.getAutoConfigEventId());
                engRequest.setEventId(newFile.getEventId());
                engRequest.setFileId(newFile.getFileId());
                
                fileEvent = helper.getEngFlowEvent(engRequest);
                log.info("Original File Type is: {}", originalFileType);
                
                updateMetadata(newFile.getEventId(), fileEvent, originalFileType);
                // Save the updated Event
                fileEvent.setEventStatus(true);
                
                newFile.setEventId(fileEvent.getEngFlowEventId());
                newFile.setFileType(originalFileType);
                newFile.setAutoConfigEventId(originalEventId);
                fileEvent.setEngFlowConfig(gson.toJson(newFile));
              }else {
                fileEvent = helper.getEngFlowEvent(newFile);
                fileEvent.setEngFlowId(savedEngFlow.getEngFlowId());
                
                log.info("Original File Type is: {}", originalFileType);
                updateMetadata(newFile.getFileId(), fileEvent, originalFileType);
                // Save the updated Event
                fileEvent.setEventStatus(true);
                
                newFile.setFileType(originalFileType);
                newFile.setAutoConfigEventId(originalEventId);
                fileEvent.setEngFlowConfig(gson.toJson(newFile));
              }
              
              fileEvent.setAutoConfigEventId(originalEventId);
              fileEvent = engFlowEventService.save(fileEvent);
              
              newFileEvents.put(originalEventId, fileEvent);
              processedEventsMap.put(originalEventId, fileEvent.getEngFlowEventId());
            }

            log.info("New file events map is: {}", newFileEvents.keySet());
            log.info(
                "Completed creating events for the newly selected files in the Automation screen.");
            
            origEngFlowConfigReq.setEngineeringId(savedEngFlow.getEngFlowId());
            origEngFlowConfigReq.setSelectedFiles(newFiles);
            log.info("Original Engineering Run config JSON after setting new files is: {} ", origEngFlowConfigReq);
            log.info("Started processing the newly selected files for Automation Run...");

            // Process further....
            origEngFlowConfigReq =
                helper.processNewFilesForAutomation(origEngFlowConfigReq, newFiles, newFileEvents, processedEventsMap);

            log.info("Processed Run Config JSON with Automation Screen selected files is: {}",
                origEngFlowConfigReq);
            log.info("Completed processing the newly selected files for Automation Run...");

            log.info(
                "Sending request to QuantumSparkAPI service for further processing (Livy Actions etc...)");
            
            savedEngFlow.setEngFlowName("AutoRun_" + savedEngFlow.getEngFlowId());
            savedEngFlow.setEngFlowDesc("AutoRun_" + savedEngFlow.getEngFlowId());

            origEngFlowConfigReq.setEngineeringId(savedEngFlow.getEngFlowId());
            savedEngFlow.setEngFlowConfig(gson.toJson(origEngFlowConfigReq));

            // Update the Eng Flow Name and Description with the newly created engFlowId...
            savedEngFlow = engFlowService.save(savedEngFlow);
            
            // Create an entry in the qsp_eng_flow_job table..
            flowJobId = createJobTableStatus(savedEngFlow.getEngFlowId(), 
                origRunConfigStr);

            // Finally send the request to QuantumSparkAPI service..
            EngRunFlowRequest engRunFlowRequest = new EngRunFlowRequest();
            engRunFlowRequest.setProjectId(newEngFlowConfigRequest.getProjectId());
            engRunFlowRequest.setEngFlowId(savedEngFlow.getEngFlowId());
            engRunFlowRequest.setFlowJobId(flowJobId);

            helper.submitEngFlowExecutionRequest(engRunFlowRequest);
          }
        }
      } else {
        joinResponse = helper.failureMessage(500,
            "Failed to fetch the Original Run Configuration file for the Project Id: "
                + newEngFlowConfigRequest.getProjectId() + " and Engineering Id: "
                + newEngFlowConfigRequest.getEngineeringId());
      }

    } catch (final Exception e) {
      log.error("Exception while performing Run Automation operation :: {}", e.getMessage());
      joinResponse = helper.failureMessage(500, "Internal Server error " + e.getMessage());
      
      updateJobStatus(newEngFlowConfigRequest.getProjectId(), flowJobId, JobRunState.FAILED.toString());
    }

    return ResponseEntity.status(HttpStatus.OK).body(joinResponse);
  }

  @PostMapping("/api/v1/ops/engflowjobhistory")
  public ResponseEntity<Object> getEngFlowJobHistory(@RequestBody EngFlowRequest engFlowRequest) {
    log.info("Engineering Job History requested for {}", engFlowRequest.toString());
    HashMap<String, Object> engJobResponse = new HashMap<>();

    try {
      helper.getProjectAndChangeSchema(engFlowRequest.getProjectId());
      List<EngFlow> engFlows = engFlowService.getEngFlows(engFlowRequest.getEngFlowId());
      List<EngFlowJobRequest> flowJobsDetails = new ArrayList<>();

      if (engFlows != null && !engFlows.isEmpty()) {
        log.info("Number of Engineering Flow with id: {} are {}", engFlowRequest.getEngFlowId(), engFlows.size());
        for (EngFlow engFlow : engFlows) {
          List<EngFlowJob> flowJobs = engFlowJobService.getJobs(engFlow.getEngFlowId());

          for (EngFlowJob job : flowJobs) {
            EngFlowJobRequest flowJob = new EngFlowJobRequest();
            flowJob.setEngFlowId(engFlow.getEngFlowId());
            flowJob.setEngFlowName(engFlow.getEngFlowName());
            flowJob.setFlowJobId(job.getFlowJobId());
            flowJob.setStatus(job.getStatus());
            flowJob.setRunDate(job.getRundate());
            flowJob.setBatchJobLog(job.getBatchJobLog());
            flowJob.setAutorunReqPayload(job.getAutorunReqPayload());
            
            flowJobsDetails.add(flowJob);
          }
        }
      }
      
      Collections.sort(flowJobsDetails, ((EngFlowJobRequest o1, EngFlowJobRequest o2) -> o2
          .getRunDate().compareTo(o1.getRunDate())));

      engJobResponse = helper.createSuccessMessage(200,
          "Successfully returned engineering jobs history!!!", flowJobsDetails);
    } catch (final Exception e) {
      log.error("Exception while retrieving Engineering Flow Job History operation :: {}",
          e.getMessage());
      engJobResponse = helper.failureMessage(500, "Internal Server error " + e.getMessage());
    }

    return ResponseEntity.status(HttpStatus.OK).body(engJobResponse);
  }

  @PostMapping("/api/v1/ops/getengflowfiles")
  public ResponseEntity<Object> getFilesToCleanse(@RequestBody DataFrameRequest dataFrameRequest) {
    log.info("List of files to cleanse requested for {}", dataFrameRequest.toString());
    HashMap<String, Object> filesToCleanseResponse = new HashMap<>();
    try {
      int projectId = dataFrameRequest.getProjectId();
      int engFlowId = dataFrameRequest.getEngFlowId();
      helper.getProjectAndChangeSchema(projectId);

      List<EngFlowEvent> fileEvents = engFlowEventService.getFileEvents(engFlowId, "file");
      log.info("File Events retrieved for Engineering flow id: {} are: {}", engFlowId, fileEvents);

      if (fileEvents != null && !fileEvents.isEmpty()) {
        // Process these event records now..
        Set<FolderInfo> foldersInfo = new HashSet<>();
        for (EngFlowEvent fileEvent : fileEvents) {
          String engFlowConfigStr = fileEvent.getEngFlowConfig();
          DataFrameRequest fileConfig = gson.fromJson(engFlowConfigStr, DataFrameRequest.class);
          int folderId = fileConfig.getFolderId();
          QsFolders folder = folderService.getFolder(folderId);

          // Get the list of files associated with the Folder Id from QSP_FILE table...
          List<QsFiles> files = fileService.getFiles(projectId, folderId);

          FolderInfo folderInfo = new FolderInfo();
          folderInfo.setFolderId(folderId);
          folderInfo.setFolderName(folder.getFolderName());
          folderInfo.setFiles(metadataHelper.getFolderCleanseInfo(folderId, fileEvents, files));

          foldersInfo.add(folderInfo);
        }

        FolderCleanseInfo folderCleanseInfo = new FolderCleanseInfo();
        folderCleanseInfo.setProjectId(projectId);
        folderCleanseInfo.setEngFlowId(engFlowId);
        folderCleanseInfo.setUserId(dataFrameRequest.getUserId());
        folderCleanseInfo.setFolders(foldersInfo);

        List<FolderCleanseInfo> folderCleanseInfoList = new ArrayList<>();
        folderCleanseInfoList.add(folderCleanseInfo);

        JsonElement folderCleanseInfoJson = gson.toJsonTree(folderCleanseInfoList);
        String folderCleanseInfoStr = gson.toJson(folderCleanseInfoJson);
        log.info("Folder cleanse instance returned is: {}", folderCleanseInfoStr);

        filesToCleanseResponse = helper.createSuccessMessage(200,
            "Successfully returned Folder cleanse history!!!", folderCleanseInfoStr);
      } else {
        log.info("There are no records in ENG_FLOW_EVENT table related to Engineering Id {}",
            dataFrameRequest.getEngFlowId());
        filesToCleanseResponse = helper.failureMessage(500,
            "No event records for the Engineering Id: {}" + dataFrameRequest.getEngFlowId());
      }

    } catch (final Exception e) {
      log.error(
          "Exception while retrieving list of files per folder to be cleansed operation :: {}",
          e.getMessage());
      filesToCleanseResponse =
          helper.failureMessage(500, "Internal Server error " + e.getMessage());
    }

    return ResponseEntity.status(HttpStatus.OK).body(filesToCleanseResponse);
  }
  
  @ApiOperation(value = "Get all files for each engg. flow event of a project", response = Json.class)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "No Content"),
        @ApiResponse(code = 500, message = "Failed to process request")
      })
  @GetMapping("/api/v1/event/{projectId}/{engFlowId}")
  public ResponseEntity<Object> getAllFilesByProjectAndFlowEvent(
      @PathVariable(value = "projectId") int projectId,
      @PathVariable(value = "engFlowId") int engFlowId) {
    Map<String, Object> response;
    try {
      List<FolderDetails> rawFilesResponse = new ArrayList<>();
      List<FolderDetails> cleansedFilesResponse = new ArrayList<>();
      List <EngFlowDetails> engineeredFilesResponse = new ArrayList<>();
      Projects project = helper.getProjects(projectId);
      List<EngFlowEvent> engFlowEventList = getEngFlowEvents(projectId, engFlowId);
      engFlowEventList.stream().forEach(
          (engFlowEvent) -> {     
            QsFolders folder = folderService.getFolder(engFlowEvent.getFolderId());
            Optional<QsFiles> files = fileService.getFileById(engFlowEvent.getFileId());
           if(QsConstants.RAW.equals(engFlowEvent.getFileType())) {
             FolderDetails folderDetails = createFolderDetails(folder, files.get(), QsConstants.RAW);
             rawFilesResponse.add(folderDetails);
           } else if(QsConstants.PROCESSED.equals(engFlowEvent.getFileType())) {
             FolderDetails folderDetails = createFolderDetails(folder, files.get(), QsConstants.PROCESSED);
             cleansedFilesResponse.add(folderDetails);
           } else if(QsConstants.ENG_OP.equals(engFlowEvent.getFileType())) {
             EngFlowDetails engFlowDetails = createEngFlowDetails(engFlowEvent);
             engineeredFilesResponse.add(engFlowDetails);
           }
          }
      );
      EngFlowEventFiles engFlowEventFiles = new EngFlowEventFiles();
      engFlowEventFiles.setProjectId(project.getProjectId());
      engFlowEventFiles.setRawFilesResponse(rawFilesResponse);
      engFlowEventFiles.setCleansedFilesResponse(cleansedFilesResponse);
      engFlowEventFiles.setEngineeredFilesResponse(engineeredFilesResponse);
      response = helper.createSuccessMessage(HttpStatus.OK.value(), "Success", engFlowEventFiles);
    } catch (final SQLException e) {
      response = helper.failureMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      log.error("Error {} and {}", e.getErrorCode(), e.getMessage());
    }
    return ResponseEntity.status((Integer)response.get("code")).body(response);
  }

  /**
   * @param folder
   * @param folderDetails
   */
  private FolderDetails createFolderDetails(QsFolders folder, QsFiles file, String category) {
    FolderDetails folderDetails = new FolderDetails();
    folderDetails.setCategory(category);
    folderDetails.setExternal(folder.isExternal());
    folderDetails.setFolderId(folder.getFolderId());
    folderDetails.setName(folder.getFolderDesc());
    folderDetails.getFileDetails().add(createFileDetails(file, folder.getFolderId(),category));
    return folderDetails;
  }
  
  private FileDetails createFileDetails(QsFiles file, int folderId, String category) {
    FileDetails fileDetails = new FileDetails();
    fileDetails.setFileId(file.getFileId());
    fileDetails.setFileName(file.getFileName());
    fileDetails.setCategory(category);
    fileDetails.setFolderId(folderId);
    fileDetails.setCreatedDate(file.getCreatedDate());
    return fileDetails;
  }
  
  /**
   * @param folder
   * @param folderDetails
   */
  private EngFlowDetails createEngFlowDetails(EngFlowEvent engFlowEvent) {
    EngFlowDetails engFlowDetails = new EngFlowDetails();
    engFlowDetails.setCategory(QsConstants.ENGINEERED);
    engFlowDetails.setType("ENG");
    
    engFlowDetails.getFileDetails().add(createEngFileDetails(engFlowEvent.getEngFlowId(),engFlowEvent.getFileType()));
    return engFlowDetails;
  }
  

  private FileDetails createEngFileDetails(int engFlowId, String fileType){
    Optional<EngFlow> engFlow = null;
    FileDetails fileDetails = new FileDetails();
    try {
      fileDetails.setFileId(engFlowId);
      fileDetails.setCategory(fileType);
      engFlow = engFlowService.getEngFlow(engFlowId);
      fileDetails.setFileName((engFlow.isPresent())?engFlow.get().getEngFlowName():"");
      fileDetails.setCreatedDate((engFlow.isPresent())?engFlow.get().getCreatedDate():QsConstants.getCurrentUtcDate());
    } catch (SQLException sqlException) {
      log.error("Error while fetching eng flow data : " + sqlException.getMessage());
    }
    return fileDetails;
  }

  /**
   * @param projectId
   * @param engFlowId
   * @throws SQLException
   */
  private List<EngFlowEvent> getEngFlowEvents(int projectId, int engFlowId) throws SQLException {
    List<String> eventTypes = new ArrayList<>();
    eventTypes.add(QsConstants.FILE_OP);
    eventTypes.add(QsConstants.ENG_OP);
    List<EngFlowEvent> engFlowEventList = engFlowEventService.getEngFlowEventByTypes(engFlowId, projectId,eventTypes);
    return engFlowEventList;
  }
  @PostMapping("/api/v1/ops/aggregate")
  public ResponseEntity<Object> handleAggregateReq(@RequestBody AggregateRequest aggRequest) {
    log.info("Aggregate Operation requested for {}", aggRequest.toString());
    HashMap<String, Object> aggResponse = new HashMap<>();

    try {
      Projects project = helper.getProjects(aggRequest.getProjectId());
      EngFlowEvent aggEvent = null;
      if (aggRequest.getEventId() == 0) {
        aggEvent = new EngFlowEvent();
        aggEvent.setEngFlowId(aggRequest.getEngFlowId());
        aggEvent.setEventType(AGG_OP);
        aggEvent = engFlowEventService.save(aggEvent);
      } else {
        Optional<EngFlowEvent> engFlowEventRecord =
            engFlowEventService.getFlowEvent(aggRequest.getEventId());
        if (engFlowEventRecord.isPresent()) {
          aggEvent = engFlowEventRecord.get();

          aggEvent.setEngFlowConfig(gson.toJson(aggRequest));
          aggEvent.setEngFlowEventData(null);
          
          // First save the updated Request..
          aggEvent = engFlowEventService.save(aggEvent);
          aggRequest.setEventId(aggEvent.getEngFlowEventId());
          aggRequest.setEngFlowId(aggEvent.getEngFlowId());
          aggRequest.setUserId(project.getUserId());
          
          helper.updateFlowEventWithAggResults(aggRequest);
          log.info("Performing Aggregate operation :: for event {} and for flow {}",
              aggEvent.getEngFlowEventId(), aggEvent.getEngFlowId());
        }
      }

      aggResponse = helper.createSuccessMessage(200, "Aggregate Action Event captured",
          aggEvent.getEngFlowEventId());
    } catch (final Exception e) {
      log.error("Exception while performing join operation :: {}", e.getMessage());
      aggResponse = helper.failureMessage(500, "Internal Server error " + e.getMessage());
    }

    return ResponseEntity.status(HttpStatus.OK).body(aggResponse);
  }

  @PostMapping("/api/v1/ops/db")
  public ResponseEntity<Object> handleDbReq(@RequestBody EngFlowEventRequest eventRequest) {
    log.info("Aggregate Operation requested for {}", eventRequest.toString());
    HashMap<String, Object> dbResponse = new HashMap<>();

    try {
      helper.getProjectAndChangeSchema(eventRequest.getProjectId());
      EngFlowEvent dbEvent = null;
      dbEvent = new EngFlowEvent();
      dbEvent.setEngFlowId(eventRequest.getEngFlowId());
      dbEvent.setEventType(DB_OP);
      dbEvent = engFlowEventService.save(dbEvent);

      dbResponse = helper.createSuccessMessage(200, "Database Action Event captured",
          dbEvent.getEngFlowEventId());
    } catch (final Exception e) {
      log.error("Exception while performing join operation :: {}", e.getMessage());
      dbResponse = helper.failureMessage(500, "Internal Server error " + e.getMessage());
    }

    return ResponseEntity.status(HttpStatus.OK).body(dbResponse);
  }
  
  @PostMapping("/api/v1/runcleansejob")
  public ResponseEntity<Object> runCleanseJob(@RequestBody CleanseJobRequest cleanseJobRequest) {
    log.info("Cleanse Job requested for {}", cleanseJobRequest.toString());
    HashMap<String, Object> response = new HashMap<>();

    try {
      Projects project = helper.getProjects(cleanseJobRequest.getProjectId());
      
      helper.getProjectAndChangeSchema(cleanseJobRequest.getProjectId());
      Optional<QsFiles> fileOp = fileService.getFileById(cleanseJobRequest.getFileId());
      QsFiles file = null;
      if(fileOp.isPresent()) {
        file = fileOp.get();
        
        RunJobStatus runJob = new RunJobStatus();
        runJob.setCreatedDate(QsConstants.getCurrentUtcDate());
        runJob.setFileId(file.getFileId());
        runJob.setFolderId(file.getFolderId());
        runJob.setUserId(project.getUserId());
        runJob.setProjectId(project.getProjectId());
        runJob.setStatus(JobRunState.STARTING.toString());
        RunJobStatus runJobStatus = runJobService.saveRunJob(runJob);
        
        // Submit the job to Quantum spark service...
        CleanseJobRequest cleanseJobReq = new CleanseJobRequest(project.getProjectId(), file.getFileId(), runJobStatus.getRunJobId());
        helper.submitCleanseJobRequest(cleanseJobReq);
        log.info("Submitting a Cleanse Job Request for Project Id {} and File Id {}",
            project.getProjectId(), file.getFileId());
        
        response = helper.createSuccessMessage(200, "Cleanse Job is running",
            runJobStatus.getRunJobId());
      } else {
        response = helper.createSuccessMessage(400, "File with Id: "+ cleanseJobRequest.getFileId()+ " not found.",
            null);
      }
      
    } catch (final Exception e) {
      log.error("Exception while performing join operation :: {}", e.getMessage());
      response = helper.failureMessage(500, "Internal Server error " + e.getMessage());
    }

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
  
  private HashMap<String, Object> livyEngOperation(EngFileOperationRequest engFileRequest) {
    log.info("Engineering file request received: {}", engFileRequest);
    HashMap<String, Object> eventResponse;
    try {
      if (engFileRequest.getProjectId() > 0) {
        helper.getProjectAndChangeSchema(engFileRequest.getProjectId());

        EngFlowEvent flowEvent = helper.getEngFlowEvent(engFileRequest);
        engFileRequest.setEventId(flowEvent.getEngFlowEventId());
        
        updateMetadata(engFileRequest.getFileId(), flowEvent, engFileRequest.getFileType());

        log.info("Updated EngFlowEvent instance :: {}", flowEvent);
        // Save the updated Event
        flowEvent = engFlowEventService.save(flowEvent);

        helper.updateFlowEventWithEngFileData(engFileRequest);
        eventResponse = helper.createSuccessMessage(200, "Action saved!",
            flowEvent.getEngFlowEventId(), flowEvent.getFileMetaData());
      } else {
        eventResponse = helper.failureMessage(400, "BadRequest !");
        log.error("Attributes cant not be zero!");
      }
    } catch (Exception exception) {
      exception.printStackTrace();
      eventResponse =
          helper.failureMessage(500, "Internal Server Error !" + exception.getMessage());
      log.error("Exception - {}", exception.getMessage());
    }

    return eventResponse;
  }

  private void updateMetadata(int fileId, EngFlowEvent flowEvent, String fileType) throws Exception{
    if(RAW.equals(fileType)) {
      Optional<QsFiles> fileById = fileService.getFileById(fileId);
      if (fileById.isPresent()) {
        flowEvent.setFileMetaData(metadataHelper.getFileMetadata(fileById.get().getQsMetaData(),
            flowEvent.getEngFlowEventId()));
      } else {
        log.warn("Unable to find Metadata for the file {}", fileId);
      }
    } else if(PROCESSED.equals(fileType)){
      List<FileMetaDataAwsRef> fileMds = fileMetaDataAwsService.getDistinctFileDesc(fileId);
      if(fileMds != null && !fileMds.isEmpty()) {
        FileMetaDataAwsRef awsRef = fileMds.get(0);
        flowEvent.setFileMetaData(metadataHelper.getFileMetadata(awsRef.getFileMetaData(),
            flowEvent.getEngFlowEventId()));
      } else {
        log.warn("Unable to find Metadata for the file {}", fileId);
      }
    } else if(ENG_OP.equals(fileType)) {
      List<EngFlow> childEngFlows = engFlowService.getChildEngFlows(fileId);
      if(childEngFlows != null && !childEngFlows.isEmpty()) {
        EngFlow childEngFlow = childEngFlows.get(0);
        EngFlowMetaDataAwsRef engFlowMetadataAwsRef = engFlowMetadataService.getByFlowId(childEngFlow.getEngFlowId());

        flowEvent.setFileMetaData(metadataHelper.getFileMetadata(engFlowMetadataAwsRef.getEngFlowMetaData(),
            flowEvent.getEngFlowEventId()));
      }
      else {
        log.warn("Unable to find Metadata for the Engineering File with Eng Flow Id {}", fileId);
      }
    }
  }
  
  private int createJobTableStatus(final int flowId, final String autorunReqPayload) {
    EngFlowJob flowJob = new EngFlowJob();
    flowJob.setEngFlowId(flowId);
    flowJob.setStatus(JobRunState.STARTING.toString());
    flowJob.setRundate(QsConstants.getCurrentUtcDate());
    flowJob.setAutorunReqPayload(autorunReqPayload);
    
    flowJob = engFlowJobService.save(flowJob);
    return flowJob.getFlowJobId();
  }
  
  private EngFlowJob updateJobStatus(final int projectId, final int flowJobId, final String status) {
    helper.getProjectAndChangeSchema(projectId);
    Optional<EngFlowJob> flowJobOp = engFlowJobService.getThisJob(flowJobId);
    EngFlowJob flowJob = null;
    if(flowJobOp.isPresent()) {
      flowJob = flowJobOp.get();
      flowJob.setStatus(status);
      flowJob = engFlowJobService.save(flowJob);
    }
    
    return flowJob;
  }
}
