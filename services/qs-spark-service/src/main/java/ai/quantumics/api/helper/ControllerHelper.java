/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.helper;

import ai.quantumics.api.constants.AuditEventType;
import ai.quantumics.api.constants.AuditEventTypeAction;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.exceptions.QuantumsparkUserNotFound;
import ai.quantumics.api.exceptions.SchemaChangeException;
import ai.quantumics.api.livy.LivyActions;
import ai.quantumics.api.model.*;
import ai.quantumics.api.req.*;
import ai.quantumics.api.service.*;
import ai.quantumics.api.util.CipherUtils;
import ai.quantumics.api.util.DbSessionUtil;
import ai.quantumics.api.util.JobRunState;
import ai.quantumics.api.util.MetadataHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONValue;
import org.springframework.core.env.Environment;
//import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static ai.quantumics.api.constants.QsConstants.*;

@Slf4j
@Component
public class ControllerHelper {
  private static final Gson gson = new Gson();
  
  private final DbSessionUtil dbUtil;
  private final UserServiceV2 userService;
  private final LivyActions livyActions;
  private final ProjectService projectService;
  private final EngFlowEventService engFlowEventService;
  //private final SimpMessageSendingOperations messagingTemplate;
  private final MetadataHelper metadataHelper;
  private final EngFlowJobService flowJobService;
  private final AuditEventsService auditEventsService;
  private final Environment applicationProperties;
  private final EngFlowService engFlowService;
  private final EngFlowDatalineageService datalineageService;
  
  public ControllerHelper(
      DbSessionUtil dbUtilCi,
      ProjectService projectServiceCi,
      UserServiceV2 userServiceCi,
      LivyActions livyActionsCi,
      EngFlowEventService engFlowEventServiceCi,
      //SimpMessageSendingOperations messagingTemplateCi,
      MetadataHelper metadataHelperCi,
      EngFlowJobService flowJobServiceCi,
      AuditEventsService auditEventsServiceCi,
      Environment applicationPropertiesCi,
      EngFlowService engFlowServiceCi,
      EngFlowDatalineageService datalineageServiceCi) {
    dbUtil = dbUtilCi;
    projectService = projectServiceCi;
    userService = userServiceCi;
    livyActions = livyActionsCi;
    engFlowEventService = engFlowEventServiceCi;
    //messagingTemplate = messagingTemplateCi;
    metadataHelper = metadataHelperCi;
    flowJobService = flowJobServiceCi;
    auditEventsService = auditEventsServiceCi;
    applicationProperties = applicationPropertiesCi;
    engFlowService = engFlowServiceCi;
    datalineageService = datalineageServiceCi;
  }

  private static String joinListWithCustomDelimiter(final ArrayList<String> tempData) {
    return String.join("','", tempData).toLowerCase();
  }

  public String getProjectAndChangeSchema(int projectId) throws SchemaChangeException {
    dbUtil.changeSchema("public");
    final Projects project = projectService.getProject(projectId);
    dbUtil.changeSchema(project.getDbSchemaName());
    return project.getDbSchemaName();
  }

  public void swtichToPublicSchema() throws SchemaChangeException {
    dbUtil.changeSchema(PUBLIC);
  }

  public Projects getProjects(int projectId) throws SchemaChangeException {
    dbUtil.changeSchema("public");
    final Projects project = projectService.getProject(projectId);
    dbUtil.changeSchema(project.getDbSchemaName());
    return project;
  }
  
  public Projects getProjects(int projectId, int userId) throws SchemaChangeException {
    dbUtil.changeSchema("public");
    final Projects project = projectService.getProject(projectId, userId);
    dbUtil.changeSchema(project.getDbSchemaName());
    return project;
  }

  public String getUserName(int userId, String schema) throws SchemaChangeException, SQLException {
    dbUtil.changeSchema("public");
    QsUserV2 userById = userService.getUserById(userId);
    dbUtil.changeSchema(schema);
    QsUserProfileV2 userProfile = userById.getQsUserProfile();
    return getFullName(userProfile);
  }

  /**
   * @param userById
   * @return
   */
  public String getFullName(QsUserProfileV2 userProfile) {
    return (userProfile != null) ? getFullName(userProfile.getUserFirstName(), userProfile.getUserLastName())
        : "";
  }
  
  /*only for public schema*/
  public String getUserName(int userId) throws SchemaChangeException, SQLException {
    dbUtil.changeSchema("public");
    QsUserV2 userById = userService.getUserById(userId);
    QsUserProfileV2 userProfile = userById.getQsUserProfile();
    return (userProfile != null) ? getFullName(userProfile.getUserFirstName(), userProfile.getUserLastName()) : "";
  }
 
  public HashMap<String, Object> failureMessage(final int code, final String message) {
    final HashMap<String, Object> success = new HashMap<>();
    success.put("code", code);
    success.put("message", message);
    return success;
  }

  public HashMap<String, Object> createSuccessMessage(
      final int code, final String message, Object result) {
    final HashMap<String, Object> success = new HashMap<>();
    success.put("code", code);
    success.put("result", result);
    success.put("message", message);
    
    return success;
  }

  public void updateFlowEventWithJoinResults(
      JoinOperationRequest wsJoinRequest, EngFlowEvent flowEvent) {
    try {
      // flowEvent.setJoinOperations(wsJoinRequest.getJoinOperations());
      getProjectAndChangeSchema(wsJoinRequest.getProjectId());

      // Fetch the File Metadata based on the EventId1 and EventId2 values available in the
      // wsJoinRequest instance.
      // Pass these file metadata JSON strings and merge them to get the JSON Metadata which is
      // finally stored in
      // ENG_FLOW_EVENT table in file_meta_data column.

      Optional<EngFlowEvent> efe1 = engFlowEventService.getFlowEvent(wsJoinRequest.getEventId1());
      EngFlowEvent engFlowEvent1 = null;
      String file1Metadata = "";
      if (efe1.isPresent()) {
        engFlowEvent1 = efe1.get();
        file1Metadata = engFlowEvent1.getFileMetaData();
      }

      log.info("File1 Metadata retrieved from ENG_FLOW_EVENT table is: {}", file1Metadata);

      Optional<EngFlowEvent> efe2 = engFlowEventService.getFlowEvent(wsJoinRequest.getEventId2());
      EngFlowEvent engFlowEvent2 = null;
      String file2Metadata = "";
      if (efe2.isPresent()) {
        engFlowEvent2 = efe2.get();
        file2Metadata = engFlowEvent2.getFileMetaData();
      }

      log.info("File2 Metadata retrieved from ENG_FLOW_EVENT table is: {}", file2Metadata);
      String joinMetadata =
          metadataHelper.getJoinMetadata(
              flowEvent.getEngFlowEventId(), file1Metadata, file2Metadata);
      log.info("Join Metadata processed from the above two files is: {}", joinMetadata);

      JsonNode jsonNode = livyActions.invokeJoinOperation(wsJoinRequest, false, 0, null, (flowEvent.getEngFlowEventData() != null));
      String joinEventData = "";
      if(jsonNode != null) {
        joinEventData = jsonNode.toString();
        
        // Fetch the EngFlowEvent object again as it is updated by the above Livy Operation.
        Optional<EngFlowEvent> joinEventOp = engFlowEventService.getFlowEvent(flowEvent.getEngFlowEventId());
        if(joinEventOp.isPresent()) {
          flowEvent = joinEventOp.get();
        }
        
        flowEvent.setFileMetaData(joinMetadata);
        flowEvent.setEngFlowEventData(joinEventData);
        flowEvent.setEventProgress(100.00);
        
        getProjectAndChangeSchema(wsJoinRequest.getProjectId());
        engFlowEventService.save(flowEvent);
      }
    } catch (Exception exception) {
      /*messagingTemplate.convertAndSend(
          "/queue/errors", "Error Occurred while processing " + wsJoinRequest.toString());*/
    }
  }
  
  public void updateFlowEventWithUdfResults(UdfOperationRequest wsUdfRequest, EngFlowEvent flowEvent) {
	  try {
		  // flowEvent.setJoinOperations(wsUdfRequest.getJoinOperations());
		  getProjectAndChangeSchema(wsUdfRequest.getProjectId());

		  // Fetch the File Metadata based on the EventId1 and EventId2 values available in the
		  // wsUdfRequest instance.
		  // Pass these file metadata JSON strings and merge them to get the JSON Metadata which is
		  // finally stored in
		  // ENG_FLOW_EVENT table in file_meta_data column.

		  Map<String, Integer> eventIds = wsUdfRequest.getEventIds();
		  int eventId = eventIds.get("eventId");
		  List<Metadata> udfCols = new ArrayList<>();
		  for(Map.Entry<String, Integer> events : eventIds.entrySet()) {
			  if(!"eventId".equals(events.getKey())) {
				  Optional<EngFlowEvent> efe = engFlowEventService.getFlowEvent(events.getValue());
				  EngFlowEvent engFlowEvent = null;
				  String fileMetadata = "";
				  if (efe.isPresent()) {
					  engFlowEvent = efe.get();
					  fileMetadata = engFlowEvent.getFileMetaData();
					  log.info("File Metadata retrieved from ENG_FLOW_EVENT table is: {}", fileMetadata);
				  }
					
					FileJoinColumnMetadata fileColumnMetadata = gson.fromJson(fileMetadata, FileJoinColumnMetadata.class);	
					udfCols.addAll(fileColumnMetadata.getMetadata());	
			  }
		  }
		    
		  FileJoinColumnMetadata udfMetadata = new FileJoinColumnMetadata();
		  udfMetadata.setEventId(eventId);
		  udfMetadata.setMetadata(udfCols);
		  String udfColumnMetadata = gson.toJson(udfMetadata, FileJoinColumnMetadata.class);

		  //String joinMetadata = metadataHelper.getJoinMetadata(flowEvent.getEngFlowEventId(), file1Metadata, file2Metadata);
		  log.info("Join Metadata processed from the above two files is: {}", udfColumnMetadata);

		  JsonNode jsonNode = livyActions.invokeUdfOperation(wsUdfRequest, false, 0, null, (flowEvent.getEngFlowEventData() != null), null);
		  String udfEventData = "";
		  if(jsonNode != null) {
			  udfEventData = jsonNode.toString();

			  // Fetch the EngFlowEvent object again as it is updated by the above Livy Operation.
			  Optional<EngFlowEvent> udfEventOp = engFlowEventService.getFlowEvent(flowEvent.getEngFlowEventId());
			  if(udfEventOp.isPresent()) {
				  flowEvent = udfEventOp.get();
			  }

			  flowEvent.setFileMetaData(udfColumnMetadata);
			  flowEvent.setEngFlowEventData(udfEventData);
			  flowEvent.setEventProgress(100.00);

			  getProjectAndChangeSchema(wsUdfRequest.getProjectId());
			  engFlowEventService.save(flowEvent);
		  }
	  } catch (Exception exception) {
		  /*messagingTemplate.convertAndSend(
				  "/queue/errors", "Error Occurred while processing " + wsUdfRequest.toString());*/
	  }
  }

  public int runEngJobFileOpHandler(
      final List<DataFrameRequest> fileOps, Projects projects, final int flowId, final int flowJobId, StringBuilder fileContents)
      throws SQLException {
    
    int eventId1 = -1;
    
    for (DataFrameRequest dataFrameRequest : fileOps) {
      Optional<EngFlowEvent> engFlowEventOp =
          engFlowEventService.getFlowEvent(dataFrameRequest.getEventId());
      EngFlowEvent flowEvent = null;
      if (engFlowEventOp.isPresent()) {
        flowEvent = engFlowEventOp.get();
        
        eventId1 = flowEvent.getEngFlowEventId();
        
        if(!QsConstants.ENG_OP.equals(dataFrameRequest.getFileType())) {
          FileJoinColumnMetadata fileJoinColMetadata =
              gson.fromJson(flowEvent.getFileMetaData(), FileJoinColumnMetadata.class);
          log.info("File Metadata is: {}", fileJoinColMetadata);

          dataFrameRequest.setEventId(flowEvent.getEngFlowEventId());
          dataFrameRequest.setMetadata(fileJoinColMetadata);
          dataFrameRequest.setAutoConfigEventId(flowEvent.getAutoConfigEventId());
          dataFrameRequest.setEngFlowId(flowId);
          
          log.info("Updated File input :: {}", dataFrameRequest.toString());

          JsonNode jsonNode = livyActions.initLivyContext(dataFrameRequest, true, projects, flowJobId, fileContents, true); 
          if(jsonNode != null) {
            updateFlowEventDataForFileRunOnly(dataFrameRequest, jsonNode);
          }
          
        } else {
          Optional<EngFlow> engflowOp = engFlowService.getFlowsForProjectAndId(projects.getProjectId(), dataFrameRequest.getFileId());
          EngFlow engFlow = null;
          if(engflowOp.isPresent()) {
            engFlow = engflowOp.get();
          }
          
          EngFileOperationRequest engFileOpReq = new EngFileOperationRequest();
          engFileOpReq.setEventId(dataFrameRequest.getEventId());
          engFileOpReq.setFileType(dataFrameRequest.getFileType());
          engFileOpReq.setEngFlowName(engFlow.getEngFlowName());
          
          log.info("Updated File input :: {}", dataFrameRequest.toString());

          JsonNode jsonNode = livyActions.initLivyContext(engFileOpReq, true, projects, flowJobId, fileContents, false); 
          if(jsonNode != null) {
            updateFlowEventDataForFileRunOnly(dataFrameRequest, jsonNode);
          }
        }
      }
    }
    
    updateJobTableStatus(flowJobId, JobRunState.RUNNING.toString(), -1, null, -1);
    return eventId1;
  }

  public int runEngJobJoinOpHandler(
      final List<JoinOperationRequest> joinOps, Projects projects, final int flowId, int jobId, StringBuilder fileContents)
      throws Exception {

    int eventId1 = -1;
    for (JoinOperationRequest joinOperationRequest : joinOps) {
      JsonNode jsonNode = livyActions.invokeJoinOperation(joinOperationRequest, true, jobId, fileContents, false);

      joinOperationRequest.setEngFlowId(flowId);
      eventId1 = joinOperationRequest.getEventId();
      
      if(jsonNode != null) {
        updateFlowEventDataForJoinRunOnly(joinOperationRequest, jsonNode);
      }
    }
    
    log.info("Final Event Id is: {}", eventId1);
    return eventId1;
  }
  
  public int runEngJobUdfOpHandler(
		  final List<UdfOperationRequest> udfOps, Projects projects, final int flowId, int jobId, StringBuilder fileContents)
				  throws Exception {
	  int eventId1 = -1;
	  EngFlowEvent udfAction = null;
	  for (UdfOperationRequest udfOperationRequest : udfOps) {
		  getProjectAndChangeSchema(udfOperationRequest.getProjectId());
		  Optional<EngFlowEvent> engFlowEventRecord =
		            engFlowEventService.getFlowEvent(udfOperationRequest.getAutoConfigEventId());
		        if (engFlowEventRecord.isPresent()) {
		          udfAction = engFlowEventRecord.get();
		        }
		 
		  JsonNode jsonNode = livyActions.invokeUdfOperation(udfOperationRequest, true, jobId, 
				  fileContents, false, udfAction.getJoinOperations());

		  udfOperationRequest.setEngFlowId(flowId);
		  Map<String, Integer> event = udfOperationRequest.getEventIds();
		  eventId1 = event.get("eventId");

		  if(jsonNode != null) {
			  updateFlowEventDataForUdfRunOnly(udfOperationRequest, jsonNode);
		  }
	  }
	  log.info("Final Event Id from Udf is: {}", eventId1);
	  return eventId1;
  }

  public int runEngJobAggOpHandler(
      final List<AggregateRequest> aggRequests,
      Projects projects,
      final int flowId,
      final int jobId, StringBuilder fileContents)
      throws Exception {

    int eventId1 = -1;
    for (AggregateRequest aggRequest : aggRequests) {
      log.info("AggregateRequest Instance is: {}", aggRequest);
      JsonElement activity = getActivity(aggRequest, fileContents, false);
      JsonNode readTree = readableJson(activity);
      eventId1 = aggRequest.getEventId();
      
      if(readTree != null) {
        updateEventDataAndType(eventId1, flowId, readTree);
      }
    }
    
    log.info("Final Event Id from Aggregates is: {}", eventId1);
    return eventId1;
  }
  
  public EngFlowEvent updateEngFileEventWithData(
      final EngFileOperationRequest engFileOperation, EngFlowEvent flowEvent, Projects projects, final int flowId)
      throws SQLException {
    
    try {
      final JsonNode jsonNode = livyActions.initLivyContext(engFileOperation, false, projects, 0, null, false);
      String fileEventData = jsonNode.toString();
      
      fileEventData =
          fileEventData.replaceAll(
              "a_[0-9]*_", ""); // Remove a_<event_id>_ prefix for the column names..
      flowEvent.setEngFlowEventData(fileEventData);

      flowEvent = engFlowEventService.save(flowEvent);
      log.info(
          "Updated Flow Event Data into the record for {} for flow {}",
          flowEvent.getEngFlowEventId(),
          flowEvent.getEngFlowId());
    } catch (Exception exception) {
      log.error("Exception while processing {}", exception.getMessage());
    } finally {
      log.info("#########################################################");
      log.info("File Execution Completed");
      log.info("#########################################################");
    }
    return flowEvent;
  }

  public JsonNode readableJson(JsonElement activity) throws JsonProcessingException {
    final ObjectMapper mapper = new ObjectMapper();
    JsonNode readTree = null;
    
    if(activity != null) {
      final String atr = JSONValue.parse(activity.toString()).toString();
      readTree = mapper.readTree(atr);
    }
    
    return readTree;
  }

  public EngFlowEvent updateEventDataAndType(int eventId, int engFlowId, JsonNode readTree)
      throws Exception {
    Optional<EngFlowEvent> flowEvent = engFlowEventService.getFlowEvent(eventId);
    if (flowEvent.isPresent()) {
      EngFlowEvent flowEvent1 = flowEvent.get();
      
      String aggMetadata = getAggregateMetadata(eventId, readTree);
      log.info("Agg Metadata: {}", aggMetadata);
      //removeHeaderNode(readTree);
      flowEvent1.setEngFlowEventData(readTree.toString().replaceAll("a_[0-9]*_", ""));
      flowEvent1.setEventType(AGG);
      flowEvent1.setEngFlowId(engFlowId);

      // Get the Aggregate Metadata from the second node in the result. This information is updated
      // for Aggregate Event in the file_meta_data column..
      flowEvent1.setFileMetaData(aggMetadata);
      log.info("Saved event data...");
      
      return engFlowEventService.save(flowEvent1);
    } else {
      throw new RuntimeException("No Records for the events Found");
    }
  }

  private String getAggregateMetadata(int eventId, JsonNode readTree) throws Exception {
    // Since the first node is empty, to get the Column Metadata, use the second node..
    JsonNode firstNode = readTree.get(1);
    if (firstNode == null) {
      return "";
    }

    FileJoinColumnMetadata aggregateMetadata = new FileJoinColumnMetadata();
    aggregateMetadata.setEventId(eventId);

    List<Metadata> metadataList = new ArrayList<>();
    final ObjectMapper mapper = new ObjectMapper();
    @SuppressWarnings("unchecked")
    Map<String, Object> values = mapper.readValue(firstNode.toString(), Map.class);
    if (values != null && !values.isEmpty()) {
      for (String key : values.keySet()) {
        Metadata metadata = new Metadata();
        metadata.setKey(key);
        metadata.setValue(key.substring(key.lastIndexOf("_") + 1));

        metadataList.add(metadata);
      }
    }
    aggregateMetadata.setMetadata(metadataList);

    return gson.toJson(aggregateMetadata);
  }

  private void updateFlowEventDataForJoinRunOnly(
      JoinOperationRequest joinOperationRequest, JsonNode jsonNode) throws SQLException {
    getProjects(joinOperationRequest.getProjectId());
    
    Optional<EngFlowEvent> flowEvent =
        engFlowEventService.getFlowEvent(joinOperationRequest.getEventId());
    if (flowEvent.isPresent()) {
      EngFlowEvent flowEvent1 = flowEvent.get();
      
      String joinEventData = jsonNode.toString();
      joinEventData =
          joinEventData.replaceAll("a_[0-9]*_", ""); // Remove the a_<event_id>_ prefix before updating the event table..
      flowEvent1.setEngFlowEventData(joinEventData);
      flowEvent1.setEngFlowConfig(gson.toJson(joinOperationRequest));
      flowEvent1.setEngFlowId(joinOperationRequest.getEngFlowId());

      engFlowEventService.save(flowEvent1);
      
    } else {
      log.error("Error - Run Engineering data update failed {}", joinOperationRequest.toString());
    }
  }
  
  private void updateFlowEventDataForUdfRunOnly(
	      UdfOperationRequest udfOperationRequest, JsonNode jsonNode) throws SQLException {
	    getProjects(udfOperationRequest.getProjectId());
	    
	    Map<String, Integer> eventIds = udfOperationRequest.getEventIds();
	    int eventId = eventIds.get("eventId");
	    Optional<EngFlowEvent> flowEvent =
	        engFlowEventService.getFlowEvent(eventId);
	    if (flowEvent.isPresent()) {
	      EngFlowEvent flowEvent1 = flowEvent.get();
	      
	      String udfEventData = jsonNode.toString();
	      udfEventData =
	    		  udfEventData.replaceAll("a_[0-9]*_", ""); // Remove the a_<event_id>_ prefix before updating the event table..
	      flowEvent1.setEngFlowEventData(udfEventData);
	      flowEvent1.setEngFlowConfig(gson.toJson(udfOperationRequest));
	      flowEvent1.setEngFlowId(udfOperationRequest.getEngFlowId());

	      engFlowEventService.save(flowEvent1);
	      
	    } else {
	      log.error("Error - Run Engineering data update failed {}", udfOperationRequest.toString());
	    }
	  }

  private void updateFlowEventDataForFileRunOnly(
      DataFrameRequest dataFrameRequest, JsonNode jsonNode) throws SQLException {
    getProjects(dataFrameRequest.getProjectId());
    
    Optional<EngFlowEvent> flowEvent =
        engFlowEventService.getFlowEvent(dataFrameRequest.getEventId());
    if (flowEvent.isPresent()) {
      EngFlowEvent flowEvent1 = flowEvent.get();
      
      String fileEventData = jsonNode.toString();
      fileEventData = fileEventData.replaceAll("a_[0-9]*_", ""); // Remove a_<event_id>_ prefix for the column names..
      flowEvent1.setEngFlowEventData(fileEventData);
      flowEvent1.setEngFlowConfig(gson.toJson(dataFrameRequest));
      flowEvent1.setEngFlowId(dataFrameRequest.getEngFlowId());
      
      engFlowEventService.save(flowEvent1);
      
    } else {
      log.error("Error - Run Engineering data update failed {}", dataFrameRequest.toString());
    }
  }
  
  @SuppressWarnings("unused")
  private void updateFlowEventWithEngFileData(
      EngFileOperationRequest engFileOpRequest, JsonNode jsonNode) throws SQLException {
    getProjects(engFileOpRequest.getProjectId());
    Optional<EngFlowEvent> flowEvent =
        engFlowEventService.getFlowEvent(engFileOpRequest.getEventId());
    if (flowEvent.isPresent()) {
      EngFlowEvent flowEvent1 = flowEvent.get();
      String fileEventData = jsonNode.toString();
      fileEventData =
          fileEventData.replaceAll(
              "a_[0-9]*_", ""); // Remove a_<event_id>_ prefix for the column names..
      flowEvent1.setEngFlowEventData(fileEventData);
      flowEvent1.setEngFlowConfig(gson.toJson(engFileOpRequest));
      flowEvent1.setEngFlowId(engFileOpRequest.getEngFlowId());

      engFlowEventService.save(flowEvent1);
    } else {
      log.error("Error - Run Engineering data update failed {}", engFileOpRequest.toString());
    }
  }

  public void updateJobTableStatus(final int flowJobId, String status, final int batchJobId, final String batchJobLog, final long batchJobDuration) {
    EngFlowJob flowJob = null;
    final Optional<EngFlowJob> thisJob = flowJobService.getThisJob(flowJobId);
    if (thisJob.isPresent()) {
      flowJob = thisJob.get();
      flowJob.setStatus(status);
      flowJob.setBatchJobId(batchJobId);
      flowJob.setBatchJobLog(batchJobLog);
      flowJob.setBatchJobDuration(batchJobDuration);
      
      flowJobService.save(flowJob);
    } 
  }

  public EngFlowEvent updateFlowEventWithData(
      DataFrameRequest wsRequest, EngFlowEvent flowEvent, Projects project, boolean fileDataFlag) {
    EngFlowEvent save = null;
    try {
      final JsonNode jsonNode = livyActions.initLivyContext(wsRequest, false, project, 0, null, fileDataFlag);
      
      final ObjectMapper mapper = new ObjectMapper();
      String fileEventData = jsonNode.toString();
      fileEventData = fileEventData.replaceAll("a_[0-9]*_", "");
      
      JsonNode readTree = mapper.readTree(fileEventData);
      if(readTree != null) {
        String updatedData = readTree.toString();
        
        //Fetch the event as it is updated by the above livy actions.
        Optional<EngFlowEvent> flowEventOp = engFlowEventService.getFlowEvent(flowEvent.getEngFlowEventId());
        if(flowEventOp.isPresent()) {
          flowEvent = flowEventOp.get();
        }
        
        flowEvent.setEngFlowEventData(updatedData);
        flowEvent.setEventProgress(100.00);
        save = engFlowEventService.save(flowEvent);
      }
      
      log.info(
          "Updated Flow Event Data into the record for {} for flow {}",
          flowEvent.getEngFlowEventId(),
          flowEvent.getEngFlowId());
    } catch (Exception exception) {
      log.error("Exception while processing {}", exception.getMessage());
    } finally {
      log.info("#########################################################");
      log.info("File Execution Completed");
      log.info("#########################################################");
    }
    return save;
  }

  public EngFlowEvent getMeEngFlowEvent(final int engEventId) throws SQLException {

    Optional<EngFlowEvent> flowEvent = engFlowEventService.getFlowEvent(engEventId);
    EngFlowEvent flowEventResp;
    if (flowEvent.isPresent()) {
      flowEventResp = flowEvent.get();
    } else {
      log.error("Unable to find Record with the event ID {}", engEventId);
      throw new SQLException("Record Not Found");
    }
    return flowEventResp;
  }

  public JsonElement getActivity(AggregateRequest aggregateRequest, StringBuilder fileContents, boolean aggEventDataFlag) throws Exception {
    String aggregateQuery = prepareAggregateQuery(aggregateRequest);
    log.info("Query :: {}", aggregateQuery);
    Projects projects = getProjects(aggregateRequest.getProjectId());
    
    JsonElement jsonElement =
        livyActions.runAggregateQuery(
            aggregateQuery, projects, "df" + aggregateRequest.getEventId(), aggregateRequest.getEventId(), 
            fileContents, aggEventDataFlag);
    return jsonElement;
  }

  private String prepareAggregateQuery(AggregateRequest aggregateRequest) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder
        .append("df")
        .append(aggregateRequest.getEventId())
        .append("=")
        .append("df")
        .append(aggregateRequest.getDataFrameEventId())
        .append(".groupBy('")
        .append(
            joinListWithCustomDelimiter((ArrayList<String>) aggregateRequest.getGroupByColumns()))
        .append("')")
        .append(".agg(");
    List<AggReqColumn> columns = aggregateRequest.getColumns();
    for (AggReqColumn column : columns) {
      if(column.getOpName()  != null) {
        queryBuilder
        .append("f.")
        .append(column.getOpName().toLowerCase())
        .append("('")
        .append(column.getColumnName().toLowerCase())
        .append("')")
        .append(".alias")
        .append("('")
        .append(column.getAliasName().toLowerCase())
        .append("'),");
      }
    }
    int i = queryBuilder.lastIndexOf(",");
    queryBuilder.deleteCharAt(i);
    queryBuilder.append(")");
    return queryBuilder.toString();
  }
  
  public void readLinesFromTemplate(StringBuilder fileContents) throws Exception {
    File contentSource = ResourceUtils.getFile("./"+QsConstants.QS_LIVY_TEMPLATE_ENG_NAME);
    log.info("File in classpath Found {} : ", contentSource.exists());
    fileContents.append(new String(Files.readAllBytes(contentSource.toPath())));
  }
  
  public void etlScriptVarsInit(StringBuilder fileContents, String engDb) {
    fileContents.toString().replace(DB_NAME, String.format("'%s'", engDb));
  }
  
  public void recordAuditEvent(AuditEventType auditEventType, AuditEventTypeAction auditEventTypeAction,
    String auditMessage, String notificationMessage, Integer userId, Integer projectId) {
    try {
    AuditEvents auditEvents = createAuditEventModel(auditEventType, auditEventTypeAction,
        auditMessage, notificationMessage, userId, projectId); 
    if(!dbUtil.isPublicSchema()) {
      dbUtil.changeSchema("public");
    }
      auditEventsService.saveAuditEvents(auditEvents);
    } catch (SQLException sqlException) {
      log.error("Error while saving the auditevent."+ sqlException.getMessage());
    }
    
  }

  /**
   * @param auditEventType
   * @param auditEventTypeAction
   * @param auditMessage
   * @param notificationMessage
   * @param userId
   * @param projectId
   * @return
   */
  private AuditEvents createAuditEventModel(AuditEventType auditEventType,
      AuditEventTypeAction auditEventTypeAction, String auditMessage, String notificationMessage,
      Integer userId, Integer projectId) {
    AuditEvents auditEvents = new AuditEvents();
    auditEvents.setActive(true);
    auditEvents.setNotify(notificationMessage!=null);
    auditEvents.setNotificationMessage(notificationMessage);
    auditEvents.setAuditMessage(auditMessage);    
    auditEvents.setCreationDate(QsConstants.getCurrentUtcDate());
    auditEvents.setProjectId(projectId);
    auditEvents.setUserId(userId);
    auditEvents.setEventType(auditEventType.getEventType());
    auditEvents.setEventTypeAction(auditEventTypeAction.getAction());
    try {
      auditEvents.setUserName(getUserName(userId));
    } catch (SchemaChangeException | SQLException runtimeException) {
      log.error(String.
          format("Error while fetching the user details for the id: %1$s. The error is %2$s",userId,
              runtimeException.getMessage()));
    }
    return auditEvents;
  }
  
  public String getFullName(String firstName, String lastName) {
    StringBuffer fullName = new StringBuffer(firstName);
    if(lastName != null) {
      fullName.append(" ");
      fullName.append(lastName);
    }
    return fullName.toString();
  }
  
  public String getAuditMessage(AuditEventType auditEventType, AuditEventTypeAction auditEventTypeAction) {
    StringBuffer generatedKey = new StringBuffer("audit");
    generatedKey.append(QsConstants.PERIOD)
    .append(auditEventType.getEventType().toLowerCase()).append(QsConstants.PERIOD)
    .append(auditEventTypeAction.getAction().toLowerCase()).append(QsConstants.PERIOD)
    .append("auditMessage");
    return applicationProperties.getProperty(generatedKey.toString());
  }
  
  public String getNotificationMessage(AuditEventType auditEventType, AuditEventTypeAction auditEventTypeAction) {
    StringBuffer generatedKey = new StringBuffer("audit");
    generatedKey.append(QsConstants.PERIOD)
    .append(auditEventType.getEventType().toLowerCase()).append(QsConstants.PERIOD)
    .append(auditEventTypeAction.getAction().toLowerCase()).append(QsConstants.PERIOD)
    .append("notificationMsg");
    return applicationProperties.getProperty(generatedKey.toString());
  }

  public String getAuditMessage(String eventType, String action) {
    StringBuffer generatedKey = new StringBuffer("audit");
    generatedKey.append(QsConstants.PERIOD)
    .append(eventType).append(QsConstants.PERIOD)
    .append(action).append(QsConstants.PERIOD)
    .append("auditMessage");
    return applicationProperties.getProperty(generatedKey.toString().toLowerCase());
  }
  
  public String getNotificationMessage(String eventType, String action) {
    StringBuffer generatedKey = new StringBuffer("audit");
    generatedKey.append(QsConstants.PERIOD)
    .append(eventType).append(QsConstants.PERIOD)
    .append(action).append(QsConstants.PERIOD)
    .append("notificationMsg");
    return applicationProperties.getProperty(generatedKey.toString().toLowerCase());
  }
  
  public String enrichFileName(String originalFileName, Object enrichText, String separator) {
    String filename = stripFileNameExtn(originalFileName);
    String extn = getFileNameExtn(originalFileName);
    return filename+separator+enrichText+QsConstants.PERIOD+extn;
  }

  /**
   * @param originalFileName
   */
  public String stripFileNameExtn(String originalFileName) {
    int periodPointer = originalFileName.lastIndexOf(".");
    return originalFileName.substring(0, periodPointer);
  }
  
  /**
   * @param originalFileName
   */
  public String getFileNameExtn(String originalFileName) {
    int periodPointer = originalFileName.lastIndexOf(".");
    return originalFileName.substring(periodPointer+1);
  }
  
  public void initializeLivySilently(int userId) {
    livyActions.initializeLivyAsync(userId);
  }
  
  public void initializeLivy(int userId) {
    livyActions.initializeLivy(userId);
  }
  
  public boolean deleteLivySession(String sessionName) throws Exception{
    return livyActions.deleteLivySession(sessionName);
  }
  
  public void cleanupLivySessionMemory(Projects projects, int engFlowId) throws SQLException{
    List<EngFlowEvent> events = engFlowEventService.getAllEventsData(engFlowId);
    if(events != null && !events.isEmpty()) {
      
      log.info("Started cleaning up the dataframes associated with: {} events of the Engineering Flow with Id: {}", events.size(), engFlowId);
      
      StringBuilder sb = new StringBuilder();
      events.stream().forEach((event) -> {
        sb.append("df").append(event.getEngFlowEventId()).append("=None").append("\n");
      });
      
      sb.append("spark.sparkContext._jvm.System.gc()\n");
      livyActions.cleanupLivySessionMemory(projects, sb.toString());
      
      log.info("Completed cleaning up of the dataframes in the Livy Session...");
    }
  }
  
  public void insertDataLineageInfo(int engFlowId, EngFlowConfigRequest engFlowConfigRequest) throws SQLException{
    if(engFlowConfigRequest == null) return;
    
    List<DataFrameRequest> selectedFiles = engFlowConfigRequest.getSelectedFiles();
    List<JoinOperationRequest> selectedJoins = engFlowConfigRequest.getSelectedJoins();
    List<AggregateRequest> selectedAggs = engFlowConfigRequest.getSelectedAggregates();
    
    Map<Integer, EngFlowDatalineage> dataLineageValues = new LinkedHashMap<>();
    if(selectedFiles != null && !selectedFiles.isEmpty()) {
      selectedFiles.stream().forEach((file) -> {
        String eventType = QsConstants.FILE_OP;
        try {
          Optional<EngFlowEvent> eventOp = engFlowEventService.getFlowEvent(file.getEventId());
          if(eventOp.isPresent()) {
            EngFlowEvent event = eventOp.get();
            eventType = event.getEventType();
          }
        }catch(SQLException e) {
          log.info("Exception occurred while retrieving event type, defaulting to 'file'");
        }
        
        EngFlowDatalineage dataLineage = new EngFlowDatalineage();
        dataLineage.setEngFlowId(engFlowId);
        dataLineage.setEventId(file.getEventId());
        dataLineage.setEventMappingId(0);
        dataLineage.setEventType(eventType);
        dataLineage.setJoinType(null);
        dataLineage.setAttributes(null);
        dataLineage.setRecordCount(0);
        
        dataLineageValues.put(file.getEventId(), dataLineage);
      });
    }
    
    if(selectedJoins !=null && !selectedJoins.isEmpty()) {
      int count = 0;
      for(JoinOperationRequest join : selectedJoins) {
        handleJoinObject(engFlowId, dataLineageValues, join, ++count);
      }
    }
    
    if(selectedAggs != null && !selectedAggs.isEmpty()) {
      selectedAggs.stream().forEach((agg) -> {
        if(dataLineageValues.containsKey(agg.getDataFrameEventId())) {
          EngFlowDatalineage fileOrJoindataLineage = dataLineageValues.get(agg.getDataFrameEventId());
          fileOrJoindataLineage.setEventMappingId(agg.getEventId());
          fileOrJoindataLineage.setJoinType(null);
          fileOrJoindataLineage.setRecordCount(0); // Need to update it with the correct dynamic value..
        
          EngFlowDatalineage aggdataLineage = new EngFlowDatalineage();
          aggdataLineage.setEngFlowId(engFlowId);
          aggdataLineage.setEventId(agg.getEventId());
          aggdataLineage.setEventMappingId(0);
          aggdataLineage.setEventType(QsConstants.AGG);
          aggdataLineage.setJoinType(null);
          aggdataLineage.setAttributes(null);
          aggdataLineage.setRecordCount(0);
          
          dataLineageValues.put(agg.getEventId(), aggdataLineage);
        }
      });
    }
    
    // Finally insert all the Data Lineage instances into the DB..
    
    if(dataLineageValues != null && !dataLineageValues.isEmpty()) {
      Collection<EngFlowDatalineage> dataLineageObjs = dataLineageValues.values();
      datalineageService.saveAll(dataLineageObjs.stream().collect(Collectors.toList()));
    }
  }
  
  private void handleJoinObject(int engFlowId, Map<Integer, EngFlowDatalineage> dataLineageValues, JoinOperationRequest join, int count) {
    if(dataLineageValues.containsKey(join.getEventId1()) && dataLineageValues.containsKey(join.getEventId2())) {
      // This join has file1 as input, hence update the Event Mapping Id...
      EngFlowDatalineage file1dataLineage = dataLineageValues.get(join.getEventId1());
      file1dataLineage.setEventMappingId(join.getEventId());
      file1dataLineage.setRecordCount(0); // Need to update it with the correct dynamic value..
      
      EngFlowDatalineage file2dataLineage = dataLineageValues.get(join.getEventId2());
      file2dataLineage.setEventMappingId(join.getEventId());
      file2dataLineage.setRecordCount(0); // Need to update it with the correct dynamic value..
      
    }

    EngFlowDatalineage joindataLineage = new EngFlowDatalineage();
    joindataLineage.setEngFlowId(engFlowId);
    joindataLineage.setEventId(join.getEventId());
    joindataLineage.setEventMappingId(0);
    joindataLineage.setEventType(QsConstants.JOIN+count);
    joindataLineage.setJoinType(null);
    joindataLineage.setAttributes(null);
    joindataLineage.setRecordCount(0);
    
    dataLineageValues.put(join.getEventId(), joindataLineage);
  }
  
  public void dropColumnsFromCsvFile(List<Object> csvLines, String dropColumnsStr) {
    if(dropColumnsStr.isEmpty()) return;
    
    log.info("Columns to be dropped from the CSV file are: {}", dropColumnsStr);
    String[] columns = dropColumnsStr.split(",");
        
    for(Object obj : csvLines) {
      Map<String, String> values = (Map<String, String>) obj;
      
      for(String column : columns) {
        values.remove(column);
      }
    }
    
    log.info("Dropped columns '{}' from the CSV file.", dropColumnsStr);
  }
  
  public void encryptColumnsOfCsvFile(List<Object> csvLines, String encryptColumnsStr) {
    if(encryptColumnsStr.isEmpty()) return;
    
    log.info("Columns to be encrypted in the CSV file are: {}", encryptColumnsStr);
    String[] columns = encryptColumnsStr.split(",");
        
    for(Object obj : csvLines) {
      Map<String, String> values = (Map<String, String>) obj;
      
      for(String column : columns) {
        values.put(column, CipherUtils.encrypt(values.get(column)));
      }
    }
    
    log.info("Encrypted columns '{}' in the CSV file.", encryptColumnsStr);
  }
  
  public void decryptColumnsOfCsvFile(List<Object> csvLines, String decryptColumnsStr) {
    if(decryptColumnsStr.isEmpty()) return;
    
    log.info("Columns to be decrypted in the CSV file are: {}", decryptColumnsStr);
    String[] columns = decryptColumnsStr.split(",");
        
    for(Object obj : csvLines) {
      Map<String, String> values = (Map<String, String>) obj;
      
      for(String column : columns) {
        values.put(column.trim(), CipherUtils.decrypt(values.get(column.trim())));
      }
    }
    
    log.info("Decrypted columns '{}' in the CSV file.", decryptColumnsStr);
  }
  
  public void processFileBeforeUpload(UploadFileRequest uploadFileRequest, File csvFile, File csvFileTmp) throws IOException{
    BufferedWriter bw = null;
    CsvMapper csvMapper = new CsvMapper();
    List<Object> readAllLines = null;
    try {
      CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
      readAllLines = csvMapper.readerFor(Map.class).with(csvSchema).readValues(new BufferedReader(new FileReader(csvFile))).readAll();
      
      if(uploadFileRequest.getDropColumns() != null && uploadFileRequest.getEncryptPiiColumns() != null) {
        String dropColumns = uploadFileRequest.getDropColumns();
        // Drop the columns from the CSV file here..
        dropColumnsFromCsvFile(readAllLines, dropColumns);
        
        String encryptPiiColumns = uploadFileRequest.getEncryptPiiColumns();
        // Encrypt the columns here..
        encryptColumnsOfCsvFile(readAllLines, encryptPiiColumns);
      }
      
      // Finally write the list of lines back to the CSV file and then upload...
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
      }
      
      bw.flush();
    }catch(IOException e) {
      log.error("Exception occurred while processing the CSV file..{}", e.getMessage());
      
      throw new IOException("Exception occurred while processing the CSV file..");
    }
    finally{
      try {
        if(bw != null) {
          bw.close();
        }
      }catch(IOException ioe) {
        // do nothing...
      }
    }
  }
  
  public boolean isAdmin(int userId) throws QuantumsparkUserNotFound{
	  boolean admin = false;
	  try {
		  QsUserV2 qsUserV2 = userService.getActiveUserById(userId);
		  if(qsUserV2 == null) {
			  throw new QuantumsparkUserNotFound("User not found");
		  } else {
			  admin = "Admin".equalsIgnoreCase(qsUserV2.getUserRole()) ? true : false;
		  }
	} catch (SQLException sqlException) {
		throw new QuantumsparkUserNotFound("Unable to fetch user details from the database. Please contact Technical support",sqlException);
	}
	  return admin;
  }
  
  public boolean isActiveUser(int userId) {
	  boolean active = false;
	  try {
		  QsUserV2 qsUserV2 = userService.getUserById(userId);
		  if(qsUserV2 != null && qsUserV2.isActive()) {
			  active = true;
		  } 
	} catch (SQLException sqlException) {
		throw new QuantumsparkUserNotFound("Unable to fetch user details from the database. Please contact Technical support",sqlException);
	}
	  return active;
  }
  
}
