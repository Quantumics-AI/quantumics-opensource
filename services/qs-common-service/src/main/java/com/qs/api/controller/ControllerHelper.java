package com.qs.api.controller;

import com.google.gson.Gson;
import com.qs.api.exception.SchemaChangeException;
import com.qs.api.model.EngFlowEvent;
import com.qs.api.model.EngRunFlowRequest;
import com.qs.api.model.Projects;
import com.qs.api.model.QsFiles;
import com.qs.api.request.*;
import com.qs.api.service.EngFlowEventService;
import com.qs.api.service.FileService;
import com.qs.api.service.ProjectService;
import com.qs.api.util.DbSessionUtil;
import com.qs.api.util.FileComparator;
import com.qs.api.util.MetadataHelper;
import com.qs.api.util.QsConstants;
import com.qs.api.vo.CleanseJobRequest;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

import static com.qs.api.util.QsConstants.*;

@Slf4j
@Component
public class ControllerHelper {

  @Value("${qs.service.end.point.url}")
  private String qsServiceEndPointUrl;

  private static final Gson gson = new Gson();
  private final DbSessionUtil dbUtil;
  private final ProjectService projectService;
  private final EngFlowEventService engFlowEventService;
  private final RestTemplate restTemplate;
  private final MetadataHelper metadataHelper;
  private final FileService fileService;

  public ControllerHelper(DbSessionUtil dbUtilCi, ProjectService projectServiceCi,
                          EngFlowEventService engFlowEventService, RestTemplate restTemplate,
                          MetadataHelper metadataHelper, FileService fileService) {
    this.dbUtil = dbUtilCi;
    this.projectService = projectServiceCi;
    this.engFlowEventService = engFlowEventService;
    this.restTemplate = restTemplate;
    this.metadataHelper = metadataHelper;
    this.fileService = fileService;
  }

  public HashMap<String, Object> createSuccessMessage(final int code, final String message,
                                                      Object result) {
    final HashMap<String, Object> success = new HashMap<>();
    success.put(QsConstants.CODE_STR, code);
    success.put(QsConstants.RESULT, result);
    success.put(QsConstants.MESSAGE, message);
    return success;
  }

  public HashMap<String, Object> createSuccessMessage(final int code, final String message,
                                                      Object result, Object metadata) {
    final HashMap<String, Object> success = new HashMap<>();
    success.put(QsConstants.CODE_STR, code);
    success.put(QsConstants.RESULT, result);
    success.put(QsConstants.MESSAGE, message);
    success.put("metadata", metadata);

    return success;
  }

  public HashMap<String, Object> failureMessage(final int code, final String message) {
    final HashMap<String, Object> success = new HashMap<>();
    success.put(QsConstants.CODE_STR, code);
    success.put(QsConstants.MESSAGE, message);
    return success;
  }

  public String getProjectAndChangeSchema(int projectId) throws SchemaChangeException {
    dbUtil.changeSchema(QsConstants.PUBLIC);
    final Projects project = projectService.getProject(projectId);
    dbUtil.changeSchema(project.getDbSchemaName());
    return project.getDbSchemaName();
  }

  public void swtichToPublicSchema() throws SchemaChangeException {
    dbUtil.changeSchema(QsConstants.PUBLIC);
  }

  public EngFlowEvent getEventForJoinAction(JoinOperationRequest wsJoinRequest) throws SQLException {
    getProjectAndChangeSchema(wsJoinRequest.getProjectId());
    final EngFlowEvent flowEvent =
            createFlowEvent(wsJoinRequest, wsJoinRequest.getEngFlowId(), JOIN, null);
    return flowEvent;
  }

  public EngFlowEvent getEventForUdfAction(UdfOperationRequest wsUdfRequest) throws SQLException {
    getProjectAndChangeSchema(wsUdfRequest.getProjectId());
    final EngFlowEvent flowEvent =
            createFlowEvent(wsUdfRequest, wsUdfRequest.getEngFlowId(), UDF, null);
    return flowEvent;
  }

  public Projects getProjects(int projectId) throws SchemaChangeException {
    dbUtil.changeSchema(QsConstants.PUBLIC);
    final Projects project = projectService.getProject(projectId);
    dbUtil.changeSchema(project.getDbSchemaName());
    return project;
  }

  public EngFlowEvent getEngFlowEvent(DataFrameRequest wsRequest) throws SQLException {
    getProjectAndChangeSchema(wsRequest.getProjectId());

    EngFlowEvent flowEvent = createFlowEvent(wsRequest, wsRequest.getEngFlowId(), FILE_OP, wsRequest.getFileType());
    return flowEvent;
  }

  public EngFlowEvent getEngFlowEvent(EngFileOperationRequest engFileRequest) throws SQLException {
    getProjectAndChangeSchema(engFileRequest.getProjectId());
    EngFlowEvent flowEvent = null;
    if ("udf".equals(engFileRequest.getFileType())) {
      flowEvent = createFlowEvent(engFileRequest, engFileRequest.getEngFlowId(), UDF, engFileRequest.getFileType());
    } else {
      flowEvent = createFlowEvent(engFileRequest, engFileRequest.getEngFlowId(), ENG_OP, engFileRequest.getFileType());
    }
    return flowEvent;
  }

  @Async("qsThreadPool")
  public void updateFlowEventWithData(DataFrameRequest wsRequest) {
    // Invoke a HTTP Request to the Current Service by passing the wsRequest
    // payload.
    ResponseEntity<String> postForEntity = null;
    try {
      URI url = new URI(qsServiceEndPointUrl + "/api/v1/ops/file");
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(QsConstants.BEARER_AUTH_TOKEN);
      HttpEntity<DataFrameRequest> request = new HttpEntity<>(wsRequest, headers);
      postForEntity = restTemplate.postForEntity(url, request, String.class);
      log.info("##### {}", postForEntity);
      HttpStatus statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        log.info("livyPostHandler Response : {}", postForEntity.getBody());
      }
    } catch (final URISyntaxException | RestClientException e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
  }

  @Async("qsThreadPool")
  public void updateFlowEventWithUdfResults(UdfOperationRequest wsUdfRequest) {
    ResponseEntity<String> postForEntity = null;
    HttpHeaders headers = new HttpHeaders();
    try {
      URI url = new URI(qsServiceEndPointUrl + "/api/v1/ops/udf");
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(QsConstants.BEARER_AUTH_TOKEN);
      HttpEntity<UdfOperationRequest> request = new HttpEntity<>(wsUdfRequest, headers);
      postForEntity = restTemplate.postForEntity(url, request, String.class);
      HttpStatus statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        log.info("livyPostHandler Response for UDF : {}", postForEntity.getBody());
      }
    } catch (final URISyntaxException | RestClientException e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
  }

  @Async("qsThreadPool")
  public void updateFlowEventWithJoinResults(JoinOperationRequest wsJoinRequest) {
    ResponseEntity<String> postForEntity = null;
    HttpHeaders headers = new HttpHeaders();
    try {
      URI url = new URI(qsServiceEndPointUrl + "/api/v1/ops/join");
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(QsConstants.BEARER_AUTH_TOKEN);
      HttpEntity<JoinOperationRequest> request = new HttpEntity<>(wsJoinRequest, headers);
      postForEntity = restTemplate.postForEntity(url, request, String.class);
      HttpStatus statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        log.info("livyPostHandler Response : {}", postForEntity.getBody());
      }
    } catch (final URISyntaxException | RestClientException e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
  }

  @Async("qsThreadPool")
  public void updateFlowEventWithAggResults(AggregateRequest aggregateReq) {
    ResponseEntity<String> postForEntity = null;
    HttpHeaders headers = new HttpHeaders();
    try {
      URI url = new URI(qsServiceEndPointUrl + "/api/v1/aggregate");
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(QsConstants.BEARER_AUTH_TOKEN);
      HttpEntity<AggregateRequest> request = new HttpEntity<>(aggregateReq, headers);
      postForEntity = restTemplate.postForEntity(url, request, String.class);
      HttpStatus statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        log.info("livyPostHandler Response : {}", postForEntity.getBody());
      }
    } catch (final URISyntaxException | RestClientException e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
  }

  @Async("qsThreadPool")
  public void updateFlowEventWithEngFileData(EngFileOperationRequest engFileRequest) {
    ResponseEntity<String> postForEntity = null;
    try {
      URI url = new URI(qsServiceEndPointUrl + "/api/v1/ops/eng");
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(QsConstants.BEARER_AUTH_TOKEN);
      HttpEntity<EngFileOperationRequest> request = new HttpEntity<>(engFileRequest, headers);
      postForEntity = restTemplate.postForEntity(url, request, String.class);
      log.info("##### {}", postForEntity);
      HttpStatus statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        log.info("livyPostHandler Response : {}", postForEntity.getBody());
      }
    } catch (final URISyntaxException | RestClientException e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
  }

  private EngFlowEvent createFlowEvent(final Object wsRequest, final int engFlowId,
                                       final String type, final String fileType) throws SQLException {

    final String requestConfig = getGson(wsRequest);
    log.info("========>Create Event request is received for the input: {}", requestConfig);

    EngFlowEvent flowEvent = new EngFlowEvent();
    flowEvent.setEngFlowConfig(requestConfig);
    flowEvent.setEngFlowId(engFlowId);
    flowEvent.setFileType(fileType);

    if (type.equals(JOIN)) {
      flowEvent.setEventType(JOIN);
    } else if (type.equals(FILE_OP)) {
      flowEvent.setEventType(FILE_OP);
    } else if (type.equals(AGG_OP)) {
      flowEvent.setEventType(AGG_OP);
    } else if (type.equals(ENG_OP)) {
      flowEvent.setEventType(ENG_OP);
    } else if (type.equals(UDF)) {
      flowEvent.setEventType(UDF);
    }

    log.info("Processing the Create Event request for the input: {}", requestConfig);

    flowEvent = engFlowEventService.save(flowEvent);
    int currentEventId = flowEvent.getEngFlowEventId();
    Date now = DateTime.now().toDate();
    DataFrameRequest tmpFileReq = null;
    JoinOperationRequest tmpJoinReq = null;
    UdfOperationRequest tmpUdfReq = null;
    AggregateRequest tmpAggReq = null;
    EngFileOperationRequest tmpEngFileReq = null;
    if (wsRequest instanceof DataFrameRequest) {
      tmpFileReq = (DataFrameRequest) wsRequest;

      // First set the AutoConfigEventId...
      // Event Id will be zero in case user performs drag and drop of file in Engineering Screen.
      // If the request is coming from Automation Run screen, it will be non-zero.
      flowEvent.setAutoConfigEventId(
              tmpFileReq.getEventId() == 0 ? currentEventId : tmpFileReq.getEventId());
      tmpFileReq.setEventId(currentEventId);
      flowEvent.setEngFlowConfig(getGson(tmpFileReq));
      flowEvent.setProjectId(tmpFileReq.getProjectId());
      flowEvent.setFolderId(tmpFileReq.getFolderId());
      flowEvent.setFileId(tmpFileReq.getFileId());
      flowEvent.setUserId(tmpFileReq.getUserId());
      flowEvent.setCreationDate(now);

    } else if (wsRequest instanceof JoinOperationRequest) {
      tmpJoinReq = (JoinOperationRequest) wsRequest;

      // First set the AutoConfigEventId...
      // Event Id will be zero in case user performs drag and drop of join in Engineering Screen.
      // If the request is coming from Automation Run screen, it will be non-zero.
      flowEvent.setAutoConfigEventId(
              tmpJoinReq.getEventId() == 0 ? currentEventId : tmpJoinReq.getEventId());
      tmpJoinReq.setAutoConfigEventId(
              tmpJoinReq.getEventId() == 0 ? currentEventId : tmpJoinReq.getEventId());
      tmpJoinReq.setEventId(currentEventId);

      flowEvent.setEngFlowConfig(getGson(tmpJoinReq));
    } else if (wsRequest instanceof AggregateRequest) {
      tmpAggReq = (AggregateRequest) wsRequest;

      // First set the AutoConfigEventId...
      // Event Id will be zero in case user performs drag and drop of join in Engineering Screen.
      // If the request is coming from Automation Run screen, it will be non-zero.
      flowEvent.setAutoConfigEventId(
              tmpAggReq.getEventId() == 0 ? currentEventId : tmpAggReq.getEventId());
      tmpAggReq.setAutoConfigEventId(tmpAggReq.getEventId() == 0 ? currentEventId : tmpAggReq.getEventId());
      tmpAggReq.setEventId(currentEventId);

      flowEvent.setEngFlowConfig(getGson(tmpAggReq));
    } else if (wsRequest instanceof EngFileOperationRequest) {
      tmpEngFileReq = (EngFileOperationRequest) wsRequest;

      flowEvent.setProjectId(tmpEngFileReq.getProjectId());
      flowEvent.setFileId(tmpEngFileReq.getFileId());
      flowEvent.setAutoConfigEventId(
              tmpEngFileReq.getEventId() == 0 ? currentEventId : tmpEngFileReq.getEventId());
      tmpEngFileReq.setEventId(currentEventId);
      tmpEngFileReq.setParentEventId(tmpEngFileReq.getParentEventId() == 0 ? currentEventId : tmpEngFileReq.getParentEventId());
      flowEvent.setEngFlowConfig(getGson(tmpEngFileReq));
    } else if (wsRequest instanceof UdfOperationRequest) {
      tmpUdfReq = (UdfOperationRequest) wsRequest;
      Map<String, Integer> eventIds = tmpUdfReq.getEventIds();
      int eventId = eventIds.get("eventId");
      // First set the AutoConfigEventId...
      // Event Id will be zero in case user performs drag and drop of join in Engineering Screen.
      // If the request is coming from Automation Run screen, it will be non-zero.
      flowEvent.setAutoConfigEventId(
              eventId == 0 ? currentEventId : eventId);
      tmpUdfReq.setAutoConfigEventId(
              eventId == 0 ? currentEventId : eventId);
      //tmpUdfReq.setEventId(currentEventId);
      eventIds.put("eventId", currentEventId);
      tmpUdfReq.setEventIds(eventIds);
      flowEvent.setEngFlowConfig(getGson(tmpUdfReq));
    }

    log.info("Updating the Event Instance with data: {}", flowEvent);
    flowEvent = engFlowEventService.save(flowEvent); // Invoking again to update the
    // autoConfigEventId column

    return flowEvent;
  }

  public EngFlowConfigRequest processNewFilesForAutomation(
          EngFlowConfigRequest origEngFlowConfigReq, List<DataFrameRequest> newFiles,
          Map<Integer, EngFlowEvent> newFileEvents, Map<Integer, Integer> processedEventsMap) throws Exception {
    getProjectAndChangeSchema(origEngFlowConfigReq.getProjectId());

    List<JoinOperationRequest> joins = origEngFlowConfigReq.getSelectedJoins();
    if (joins != null && !joins.isEmpty()) {
      log.info(
              "Started processing the joins to include newly selected files in the Original Run Config JSON...");

      for (JoinOperationRequest join : joins) {
        int eventId = join.getEventId();
        int eventId1 = join.getEventId1();
        int eventId2 = join.getEventId2();

        log.info("Event Id1: {} and Event Id2: {}", eventId1, eventId2);

        // Now get the eventId1 and eventId2 type by fetching the record from backend...
        processEventIds(join, null, newFileEvents, eventId1, 1, 1, processedEventsMap);
        processEventIds(join, null, newFileEvents, eventId2, 2, 2, processedEventsMap);

        EngFlowEvent newOrigJoinFlowEvent = createFlowEvent(join, join.getEngFlowId(), JOIN, null);
        join.setEventId(newOrigJoinFlowEvent.getEngFlowEventId());

        Optional<EngFlowEvent> efe1 = engFlowEventService.getFlowEvent(join.getEventId1());
        EngFlowEvent engFlowEvent1 = null;
        String file1Metadata = "";
        if (efe1.isPresent()) {
          engFlowEvent1 = efe1.get();
          file1Metadata = engFlowEvent1.getFileMetaData();

          log.info("File1 Metadata retrieved from ENG_FLOW_EVENT table is: {}", file1Metadata);

          if (file1Metadata == null || file1Metadata.isEmpty()) {
            log.info(
                    "File1 Metadata retrieved from ENG_FLOW_EVENT table is empty or null processing further ");
            // This should not happen as the file metadata is processed when the file is cleansed.
            // Just to ensure things proceed further, below line is added as a fallback.
            file1Metadata = getFileMetadataFromParentFile(engFlowEvent1);
            log.info("File1 Metadata retrieved from Parent enent is: {}", file1Metadata);
          }
        }

        Optional<EngFlowEvent> efe2 = engFlowEventService.getFlowEvent(join.getEventId2());
        EngFlowEvent engFlowEvent2 = null;
        String file2Metadata = "";
        if (efe2.isPresent()) {
          engFlowEvent2 = efe2.get();
          file2Metadata = engFlowEvent2.getFileMetaData();

          log.info("File2 Metadata retrieved from ENG_FLOW_EVENT table is: {}", file2Metadata);

          if (file2Metadata == null || file2Metadata.isEmpty()) {
            log.info(
                    "File2 Metadata retrieved from ENG_FLOW_EVENT table is empty or null processing further ");
            // This should not happen as the file metadata is processed when the file is cleansed.
            // Just to ensure things proceed further, below line is added as a fallback.
            file2Metadata = getFileMetadataFromParentFile(engFlowEvent2);
            log.info("File2 Metadata retrieved from Parent enent is: {}", file2Metadata);
          }
        }

        String joinMetadata = metadataHelper.getJoinMetadata(
                newOrigJoinFlowEvent.getEngFlowEventId(), file1Metadata, file2Metadata);
        log.info("Join Metadata processed from the above two files is: {}", joinMetadata);

        newOrigJoinFlowEvent.setFileMetaData(joinMetadata);
        newOrigJoinFlowEvent.setEngFlowId(origEngFlowConfigReq.getEngineeringId());
        newOrigJoinFlowEvent.setEventStatus(true);

        newOrigJoinFlowEvent = engFlowEventService.save(newOrigJoinFlowEvent);
        log.info("Updated Join event is: {}", newOrigJoinFlowEvent);

        processedEventsMap.put(eventId, newOrigJoinFlowEvent.getEngFlowEventId());
        log.info("Processed Join Events Map is: {}", processedEventsMap);
      }
      log.info("Completed processing the joins...");
    }

    List<UdfOperationRequest> udfs = origEngFlowConfigReq.getSelectedUdfs();
    if (udfs != null && !udfs.isEmpty()) {
      log.info(
              "Started processing the udf to include newly selected files in the Original Run Config JSON...");

      for (UdfOperationRequest udf : udfs) {

        //int eventId = udf.getEventId();
        //int eventId1 = udf.getEventId1();
        //int eventId2 = udf.getEventId2();

        //log.info("Event Id1: {} and Event Id2: {}", eventId1, eventId2);

        // Now get the eventId1 and eventId2 type by fetching the record from backend...
        Map<String, Integer> eventIds = udf.getEventIds();
        int eventId = eventIds.get("eventId");
        int j = 1;
        for (Map.Entry<String, Integer> entry : eventIds.entrySet()) {
          if (!"eventId".equals(entry.getKey())) {
            udfProcessEventIds(udf, newFileEvents, entry.getValue(), j, processedEventsMap);
            j++;
          }
        }
        log.info("=============================");
        //udfProcessEventIds(udf, newFileEvents, eventId1, 1);
        //udfProcessEventIds(udf, newFileEvents, eventId2, 2);

        EngFlowEvent newOrigUdfFlowEvent = createFlowEvent(udf, udf.getEngFlowId(), UDF, null);
        //udf.setEventId(newOrigUdfFlowEvent.getEngFlowEventId());
        eventIds.put("eventId", newOrigUdfFlowEvent.getEngFlowEventId());

        List<Metadata> joinCols = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : eventIds.entrySet()) {
          if (!"eventId".equals(entry.getKey())) {
            Optional<EngFlowEvent> efe = engFlowEventService.getFlowEvent(entry.getValue());
            EngFlowEvent engFlowEvent = null;
            String fileMetadata = "";
            if (efe.isPresent()) {
              engFlowEvent = efe.get();
              fileMetadata = engFlowEvent.getFileMetaData();

              log.info("File Metadata retrieved from ENG_FLOW_EVENT table is: {}", fileMetadata);

              if (fileMetadata == null || fileMetadata.isEmpty()) {
                log.info(
                        "File Metadata retrieved from ENG_FLOW_EVENT table is empty or null processing further ");
                // This should not happen as the file metadata is processed when the file is cleansed.
                // Just to ensure things proceed further, below line is added as a fallback.
                fileMetadata = getFileMetadataFromParentFile(engFlowEvent);
                log.info("File Metadata retrieved from Parent enent is: {}", fileMetadata);
              }
            }

            FileJoinColumnMetadata fileColumnMetadata = null;
            if (fileMetadata != null && !fileMetadata.isEmpty()) {
              fileColumnMetadata = gson.fromJson(fileMetadata, FileJoinColumnMetadata.class);
              log.info("File Metadata Object parsing using GSON API: {}", fileColumnMetadata.getMetadata());
              joinCols.addAll(fileColumnMetadata.getMetadata());
            }
          }
        }

        FileJoinColumnMetadata joinMetadata = new FileJoinColumnMetadata();
        joinMetadata.setEventId(eventId);
        joinMetadata.setMetadata(joinCols);

        String finalMetadataStr = gson.toJson(joinMetadata, FileJoinColumnMetadata.class);
        log.info("Final Metadata JSON for the Join is: {}", finalMetadataStr);

        //String joinMetadata = metadataHelper.getJoinMetadata(newOrigUdfFlowEvent.getEngFlowEventId(), file1Metadata, file2Metadata);
        log.info("Udf Metadata processed from the above two files is: {}", finalMetadataStr);

        newOrigUdfFlowEvent.setFileMetaData(finalMetadataStr);
        newOrigUdfFlowEvent.setEngFlowId(origEngFlowConfigReq.getEngineeringId());
        newOrigUdfFlowEvent.setEventStatus(true);

        newOrigUdfFlowEvent = engFlowEventService.save(newOrigUdfFlowEvent);
        log.info("Updated udf event is: {}", newOrigUdfFlowEvent);

        processedEventsMap.put(eventId, newOrigUdfFlowEvent.getEngFlowEventId());
        log.info("Processed udf Events Map is: {}", processedEventsMap);
      }
      log.info("Completed processing the udfs...");
    }

    log.info("Started processing the aggregates...");

    List<AggregateRequest> aggregates = origEngFlowConfigReq.getSelectedAggregates();
    if (aggregates != null && !aggregates.isEmpty()) {
      log.info(
              "Started processing the aggregates to include newly selected files in the Original Run Config JSON...");

      for (AggregateRequest aggregate : aggregates) {

        log.info("Original Aggregate Request is: {}", aggregate);

        int eventId = aggregate.getEventId();
        int eventId1 = aggregate.getDataFrameEventId(); // TODO: I think we need to change this
        // attribute to eventId1...

        // Since this eventId1 represents either a File or Join eventId, it will be already
        // processed.
        EngFlowEvent newOrigAggFlowEvent =
                createFlowEvent(aggregate, aggregate.getEngFlowId(), AGG_OP, null);

        log.info("Updated Aggregate Event is: {}", newOrigAggFlowEvent);

        aggregate.setEventId(newOrigAggFlowEvent.getEngFlowEventId());
        newOrigAggFlowEvent.setAutoConfigEventId(eventId);
        newOrigAggFlowEvent.setEventStatus(true);
        newOrigAggFlowEvent.setEngFlowId(origEngFlowConfigReq.getEngineeringId());

        newOrigAggFlowEvent = engFlowEventService.save(newOrigAggFlowEvent);

        log.info("Updated Aggregate Event after AutoConfigEventId update is: {}",
                newOrigAggFlowEvent);

        log.info("Processed Events Map before is: {}", processedEventsMap);
        processedEventsMap.put(eventId, newOrigAggFlowEvent.getEngFlowEventId());
        log.info("Processed Events Map after is: {}", processedEventsMap);

        // Get the processed eventId1 from processedEventsMap and set it to the current Aggregate
        // instance..

        aggregate.setDataFrameEventId((processedEventsMap.get(eventId1) == null) ? 0 : processedEventsMap.get(eventId1));

        log.info("Aggregate instance is: {}", aggregate);

        // Now process the columns list...
        processAggregateColumns(aggregate, processedEventsMap);

        log.info("After processing Aggregate Columns.....");
      }
    }

    log.info("Completed processing the aggregates...");

    log.info("Final updated Run Config JSON is: {}", origEngFlowConfigReq);

    return origEngFlowConfigReq;
  }

  private void processEventIds(JoinOperationRequest originalJoin,
                               JoinOperationRequest intermediateJoin, Map<Integer, EngFlowEvent> newFileEvents, int eventId,
                               int origSeq, int seq, Map<Integer, Integer> processedEventsMap) throws SQLException {
    Optional<EngFlowEvent> engFlowEventOp = engFlowEventService.getFlowEvent(eventId);
    if (engFlowEventOp.isPresent()) {
      // Process further
      EngFlowEvent efe = engFlowEventOp.get();
      String eventConfigStr = efe.getEngFlowConfig();
      if (FILE_OP.equals(efe.getEventType())) {
        if (intermediateJoin != null) {
          if (seq == 1) {
            int newEventId1 = getNewEventIdForFile(newFileEvents, eventId);
            String newFirstFileColumn =
                    getNewColumnName(newEventId1, originalJoin.getFirstFileColumn());
            intermediateJoin.setEventId1(newEventId1);
            intermediateJoin.setFirstFileColumn(newFirstFileColumn);

            originalJoin.setEventId1(newEventId1);
            originalJoin.setFirstFileColumn(newFirstFileColumn);
          } else if (seq == 2) {
            int newEventId2 = getNewEventIdForFile(newFileEvents, eventId);
            String newSecondFileColumn =
                    getNewColumnName(newEventId2, originalJoin.getSecondFileColumn());
            intermediateJoin.setEventId2(newEventId2);
            intermediateJoin.setSecondFileColumn(newSecondFileColumn);

            originalJoin.setEventId2(newEventId2);
            originalJoin.setSecondFileColumn(newSecondFileColumn);
          }
        } else {
          // It is the Original Join where we have to replace the New File Event Id:
          if (seq == 1) {
            int newEventId1 = getNewEventIdForFile(newFileEvents, eventId);
            String newFirstFileColumn =
                    getNewColumnName(newEventId1, originalJoin.getFirstFileColumn());
            originalJoin.setEventId1(newEventId1);
            originalJoin.setFirstFileColumn(newFirstFileColumn);
          } else if (seq == 2) {
            int newEventId2 = getNewEventIdForFile(newFileEvents, eventId);
            String newSecondFileColumn =
                    getNewColumnName(newEventId2, originalJoin.getSecondFileColumn());
            originalJoin.setEventId2(newEventId2);
            originalJoin.setSecondFileColumn(newSecondFileColumn);
          }
        }

      } else {
        // For Join we have to process further...
        JoinOperationRequest jor = gson.fromJson(eventConfigStr, JoinOperationRequest.class);

        if (processedEventsMap.containsKey(jor.getEventId())) {
          if (seq == 1) {
            String origFirstFileCol = originalJoin.getFirstFileColumn();
            int origEventId1 =
                    Integer.parseInt(origFirstFileCol.substring(2, origFirstFileCol.indexOf('_', 2)));
            String newFirstFileColumn =
                    getNewColumnName(processedEventsMap.get(origEventId1), origFirstFileCol);
            originalJoin.setFirstFileColumn(newFirstFileColumn);

            int newEventId1 = processedEventsMap.get(jor.getEventId());
            originalJoin.setEventId1(newEventId1);
          } else if (seq == 2) {
            String origSecondFileCol = originalJoin.getSecondFileColumn();
            int origEventId1 =
                    Integer.parseInt(origSecondFileCol.substring(2, origSecondFileCol.indexOf('_', 2)));
            String newSecondFileColumn =
                    getNewColumnName(processedEventsMap.get(origEventId1), origSecondFileCol);
            originalJoin.setSecondFileColumn(newSecondFileColumn);

            int newEventId2 = processedEventsMap.get(jor.getEventId());
            originalJoin.setEventId2(newEventId2);
          }

          return; // return the control back to the caller..
        }

        processEventIds(originalJoin, jor, newFileEvents, jor.getEventId1(), origSeq, 1,
                processedEventsMap);
        processEventIds(originalJoin, jor, newFileEvents, jor.getEventId2(), origSeq, 2,
                processedEventsMap);
      }
    }
  }


  private void udfProcessEventIds(UdfOperationRequest originalUdf, Map<Integer, EngFlowEvent> newFileEvents,
                                  int eventId, int seq, Map<Integer, Integer> processedEventsMap) throws SQLException {
    Optional<EngFlowEvent> engFlowEventOp = engFlowEventService.getFlowEvent(eventId);
    if (engFlowEventOp.isPresent()) {
      Map<String, Integer> eventIds = originalUdf.getEventIds();
      // Process further
      EngFlowEvent efe = engFlowEventOp.get();
      String eventConfigStr = efe.getEngFlowConfig(); //-- eventIds null
      if (FILE_OP.equals(efe.getEventType())) {
        int newEventId = getNewEventIdForFile(newFileEvents, eventId);
        eventIds.put("eventId" + seq, newEventId);
        originalUdf.setEventIds(eventIds);
      } else {
        log.info("eventConfigStr :" + eventConfigStr);
        UdfOperationRequest jor = gson.fromJson(eventConfigStr, UdfOperationRequest.class);
        Map<String, Integer> ieventIds = jor.getEventIds();
        if (ieventIds != null && ieventIds.size() != 0) {
          int ieventId = ieventIds.get("eventId");
          if (processedEventsMap.containsKey(ieventId)) {
            int newEventId = processedEventsMap.get(ieventId);
            eventIds.put("eventId" + seq, newEventId);
            originalUdf.setEventIds(eventIds);
            return;
          }
          udfProcessEventIds(originalUdf, newFileEvents, ieventIds.get("eventId" + seq), seq,
                  processedEventsMap);
        } else {
          int ieventId = eventId;
          if (processedEventsMap.containsKey(ieventId)) {
            int newEventId = processedEventsMap.get(ieventId);
            eventIds.put("eventId" + seq, newEventId);
            originalUdf.setEventIds(eventIds);
            return;
          }
          log.info("setting up the event ids==>" + (eventId + seq));
          udfProcessEventIds(originalUdf, newFileEvents, eventId + seq, seq,
                  processedEventsMap);
        }

      }
    }
  }

  private void processAggregateColumns(AggregateRequest aggregate,
                                       Map<Integer, Integer> processedEventsMap) {
    log.info("Started processing the columns list for the aggregate: {}", aggregate.toString());

    List<String> groupByCols = aggregate.getGroupByColumns();

    if (groupByCols != null && !groupByCols.isEmpty()) {
      log.info("Started processing the Aggregate GroupBy column(s): {}", groupByCols);
      List<String> tmpGrpByColList = new ArrayList<>();
      for (String groupByCol : groupByCols) {
        int grpByEventId = Integer.parseInt(groupByCol.substring(2, groupByCol.indexOf('_', 2)));
        String processedGrpByColName =
                getNewColumnName(processedEventsMap.get(grpByEventId), groupByCol);

        tmpGrpByColList.add(processedGrpByColName);
      }

      log.info("Processed Aggregate GroupBy column(s): {}", tmpGrpByColList);
      aggregate.setGroupByColumns(tmpGrpByColList);
    }

    List<AggReqColumn> aggColumns = aggregate.getColumns();
    if (aggColumns != null && !aggColumns.isEmpty()) {

      log.info("Started processing the Aggregate column(s): {}", aggColumns);

      for (AggReqColumn aggColumn : aggColumns) {

        String aggColName = aggColumn.getColumnName(); // Column Name of the File or Join to which
        // aggregate is linked...
        // Replace the event id in the column name with the processed eventId of the File or Join.
        log.info("Column Name to process ---->>", aggColName);
        int eventId1 = Integer.parseInt(aggColName.substring(2, aggColName.indexOf('_', 2)));
        String processedColName = getNewColumnName(processedEventsMap.get(eventId1), aggColName);
        // Set the processed column name to the aggregate column...
        aggColumn.setColumnName(processedColName);
        String aliasName = aggColumn.getAliasName();
        // Set the processed column name to the aggregate column...
        aggColumn.setAliasName(aliasName);
      }

      log.info("Processed the Aggregate column(s) list: {}", aggColumns);
    }

    aggregate.setColumns(aggColumns);
    log.info("Completed processing the columns list...");
  }

  private int getNewEventIdForFile(final Map<Integer, EngFlowEvent> newFileEvents, final int key) {
    if (newFileEvents != null && !newFileEvents.isEmpty()) {
      for (Map.Entry<Integer, EngFlowEvent> me : newFileEvents.entrySet()) {
        if (me.getKey() == key) {
          EngFlowEvent flowEvent = me.getValue();
          return flowEvent.getEngFlowEventId();
        }
      }
    }

    return -1;
  }

  private String getNewColumnName(final int eventId, final String columnName) {
    return columnName.replace(columnName.substring(2, columnName.indexOf('_', 2)), eventId + "");
  }

  private String getGson(final Object toConvert) {
    return gson.toJson(toConvert);
  }

  // Need to refactor this to a singletonclass going forward.
  public ListenableFuture<StompSession> connect() {
    Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
    List<Transport> transports = Collections.singletonList(webSocketTransport);
    SockJsClient sockJsClient = new SockJsClient(transports);
    sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());
    WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
    String url = "ws://{host}:{port}/QuantumSparkServiceAPI/wsinit";

    return stompClient.connect(url, new WebSocketHttpHeaders(), new MyHandler(), "3.8.73.74", 8090);
  }

  @Async("qsThreadPool")
  public void submitEngFlowExecutionRequest(final EngRunFlowRequest engRunFlowRequest) {
    ResponseEntity<String> postForEntity = null;
    HttpHeaders headers = new HttpHeaders();
    try {
      URI url = new URI(qsServiceEndPointUrl + "/api/v1/ops/qswsrun");
      log.info("Run job URI: {}", url.getHost());
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(QsConstants.BEARER_AUTH_TOKEN);
      HttpEntity<EngRunFlowRequest> request = new HttpEntity<>(engRunFlowRequest, headers);
      postForEntity = restTemplate.postForEntity(url, request, String.class);
      HttpStatus statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        log.info("Engineering Flow Execution Response : {}", postForEntity.getBody());
      }
    } catch (final URISyntaxException | RestClientException e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
  }

  private String getFileMetadataFromParentFile(final EngFlowEvent engFlowEvent) throws Exception {
    DataFrameRequest dfr = gson.fromJson(engFlowEvent.getEngFlowConfig(), DataFrameRequest.class);
    List<QsFiles> files = fileService.getFiles(dfr.getProjectId(), dfr.getFolderId());
    // Sort the files in Ascending order of their creation under the folder..
    String fileMetadata = "";
    if (files != null && !files.isEmpty()) {
      Collections.sort(files, new FileComparator());
      QsFiles oldestFile = files.get(0);

      // Get the metadata of this file...
      fileMetadata = metadataHelper.getFileMetadata(oldestFile.getQsMetaData(),
              engFlowEvent.getEngFlowEventId());
    }

    return fileMetadata;
  }

  private class MyHandler extends StompSessionHandlerAdapter {

    @Override
    public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
      log.info("Now connected");
    }
  }

  public String getOriginalFileType(final List<DataFrameRequest> originalFiles,
                                    final int origEventId) {
    if (originalFiles != null && !originalFiles.isEmpty()) {
      Optional<DataFrameRequest> dfrOp =
              originalFiles.stream().filter(dfr -> dfr.getEventId() == origEventId).findFirst();
      if (dfrOp.isPresent()) {
        return dfrOp.get().getFileType();
      }
    }

    return "";
  }

  @Async("qsThreadPool")
  public void submitCleanseJobRequest(CleanseJobRequest cleanseJobRequest) {
    ResponseEntity<String> postForEntity = null;
    HttpHeaders headers = new HttpHeaders();
    try {
      URI url = new URI(qsServiceEndPointUrl + "/api/v1/runjob/cleanse");
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(QsConstants.BEARER_AUTH_TOKEN);
      HttpEntity<CleanseJobRequest> request = new HttpEntity<>(cleanseJobRequest, headers);
      postForEntity = restTemplate.postForEntity(url, request, String.class);
      HttpStatus statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        log.info("livyPostHandler Response : {}", postForEntity.getBody());
      }
    } catch (final URISyntaxException | RestClientException e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
  }

  public void invokeProjectCleanupOp() {
    ResponseEntity<String> postForEntity = null;
    HttpHeaders headers = new HttpHeaders();
    try {
      URI url = new URI(qsServiceEndPointUrl + "/api/v1/projects/cleanprojects");
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(QsConstants.BEARER_AUTH_TOKEN);
      HttpEntity<Object> request = new HttpEntity<>(null, headers);
      postForEntity = restTemplate.postForEntity(url, request, String.class);
      HttpStatus statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        log.info("livyPostHandler Response : {}", postForEntity.getBody());
      }
    } catch (final URISyntaxException | RestClientException e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
  }

  @Async("qsThreadPool")
  public void startExecutePipeline(int projectId, int userId, int pipelineId) {
    // Invoke a HTTP Request to the Current Service by passing the wsRequest
    ResponseEntity<String> postForEntity = null;
    try {
      URI url = new URI(qsServiceEndPointUrl + "/api/v1/pipeline/executepipeline/" + projectId + "/" + userId + "/" + pipelineId);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(QsConstants.BEARER_AUTH_TOKEN);
      HttpEntity request = new HttpEntity(headers);
      postForEntity = restTemplate.postForEntity(url, request, String.class);
      log.info("##### {}", postForEntity);
      HttpStatus statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        log.info("livyPostHandler Response : {}", postForEntity.getBody());
      }
    } catch (final URISyntaxException | RestClientException e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
  }

}
