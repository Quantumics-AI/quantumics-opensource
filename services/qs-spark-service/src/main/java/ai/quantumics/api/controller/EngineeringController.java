/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import ai.quantumics.api.constants.AuditEventType;
import ai.quantumics.api.constants.AuditEventTypeAction;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.helper.EngHelper;
import ai.quantumics.api.model.*;
import ai.quantumics.api.req.*;
import ai.quantumics.api.service.*;
import ai.quantumics.api.util.JobRunState;
import ai.quantumics.api.util.MetadataHelper;
import ai.quantumics.api.vo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.validation.Valid;
import java.sql.SQLException;
import java.util.*;

import static ai.quantumics.api.constants.QsConstants.SERVICE_SERVER_URL_PREFIX;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

@Slf4j
@RestController
@Api(value = "QuantumSpark Service API ")
public class EngineeringController {

	@Value("${qs.service.server.host.url}")
	private String serviceServerHostUrl;

	@Value("${s3.udf.bucketName}")
	private String qsUdfBucket;

	private static final Gson gson = new Gson();
	private final ControllerHelper helper;
	private final EngHelper engHelper;
	private final MetadataHelper metadataHelper;
	private final EngFlowService engFlowService;
	private final EngGraphService engGraphService;
	private final EngFlowEventService engFlowEventService;
	private final FolderService folderService;
	private final FileService fileService;
	private final EngFlowJobService engFlowJobService;
	private final RunJobService runJobService;
	private final DatabaseSessionManager sessionManager;
	private final UdfService udfService;

	public EngineeringController(ControllerHelper helperCi, EngGraphService graphService, EngFlowService engflowservice,
			FileService fileServiceCi, EngFlowEventService engFlowEventServiceCi, MetadataHelper metadataHelperCi,
			EngHelper engHelperCi, FolderService folderServiceCi, EngFlowJobService engFlowJobServiceCi,
			RunJobService runJobServiceCi, DatabaseSessionManager sessionManagerCi, UdfService udfServiceCi) {
		helper = helperCi;
		engGraphService = graphService;
		engFlowService = engflowservice;
		engHelper = engHelperCi;
		metadataHelper = metadataHelperCi;
		engFlowEventService = engFlowEventServiceCi;
		folderService = folderServiceCi;
		fileService = fileServiceCi;
		engFlowJobService = engFlowJobServiceCi;
		runJobService = runJobServiceCi;
		sessionManager = sessionManagerCi;
		udfService = udfServiceCi;
	}

	@ApiOperation(value = "Get graph contents", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Graph rendered"),
			@ApiResponse(code = 404, message = "Not found!") })
	@GetMapping(value = "/api/v1/graph/{projectId}/{userId}/{engFlowId}")
	public ResponseEntity<Object> getXmlContent(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "engFlowId") final int engFlowId) {
		Map<String, Object> response = new HashMap<>();
		try {
			// helper.swtichToPublicSchema();
			Projects project = helper.getProjects(projectId, userId);
			if (project == null) {
				response.put("code", HttpStatus.BAD_REQUEST);
				response.put("message",
						"Requested project with Id: " + projectId + " for User with Id: " + userId + " not found.");

				return ResponseEntity.ok().body(response);
			}

			deleteAllNonTrueEvents(engFlowId);
			Optional<EngFlow> content = engFlowService.getFlowsForProjectAndId(projectId, engFlowId);
			if (content.isPresent()) {
				EngFlow engFlow = content.get();
				response = getGraphContents(engFlow);

				if (engFlow.getParentEngFlowId() == 0) {

					// Initialize only if it is not an AutoRun related Engineering flow, else, just
					// return the Graph Contents.
					try {
						// Initialize the dataframe associated with the Events present in this
						// Engineering flow.
						String engFlowConfig = engFlow.getEngFlowConfig();
						EngFlowConfigRequest engFlowConfigRequest = gson.fromJson(engFlowConfig,
								EngFlowConfigRequest.class);

						log.info("Engineering Flow Config Request Object is: {}", engFlowConfigRequest);

						// Cleanup the Livysession memory, so that the Dataframes are populated again
						// correctly...

						helper.cleanupLivySessionMemory(project, engFlowId);

						if (engFlowConfigRequest != null) {
							List<DataFrameRequest> selectedFiles = engFlowConfigRequest.getSelectedFiles();
							if (selectedFiles != null && !selectedFiles.isEmpty()) {
								for (DataFrameRequest fileRequest : selectedFiles) {
									EngFlowEvent flowEvent = helper.getMeEngFlowEvent(fileRequest.getEventId());
									FileJoinColumnMetadata fileJoinColMetadata = gson
											.fromJson(flowEvent.getFileMetaData(), FileJoinColumnMetadata.class);
									log.info("File Metadata is: {}", fileJoinColMetadata);

									fileRequest.setMetadata(fileJoinColMetadata);
									fileRequest.setAutoConfigEventId(flowEvent.getAutoConfigEventId());

									log.info("Updated File input :: {}", fileRequest.toString());

									helper.updateFlowEventWithData(fileRequest, flowEvent, project,
											(flowEvent.getEngFlowEventData() != null
													&& !flowEvent.getEngFlowEventData().isEmpty()));
								}
							}

							List<JoinOperationRequest> selectedJoins = engFlowConfigRequest.getSelectedJoins();
							if (selectedJoins != null && !selectedJoins.isEmpty()) {
								for (JoinOperationRequest joinRequest : selectedJoins) {
									Optional<EngFlowEvent> engFlowEventRecord = engFlowEventService
											.getFlowEvent(joinRequest.getEventId());
									if (engFlowEventRecord.isPresent()) {
										EngFlowEvent joinAction = engFlowEventRecord.get();
										helper.updateFlowEventWithJoinResults(joinRequest, joinAction);
										log.info("Performing join operation :: for event {} and for flow {}",
												joinAction.getEngFlowEventId(), joinAction.getEngFlowId());
									}
								}
							}

							List<AggregateRequest> selectedAggregates = engFlowConfigRequest.getSelectedAggregates();
							if (selectedAggregates != null && !selectedAggregates.isEmpty()) {
								for (AggregateRequest aggRequest : selectedAggregates) {
									Optional<EngFlowEvent> engFlowEventRecord = engFlowEventService
											.getFlowEvent(aggRequest.getEventId());
									if (engFlowEventRecord.isPresent()) {
										EngFlowEvent aggAction = engFlowEventRecord.get();
										JsonElement activity = helper.getActivity(aggRequest, null,
												(aggAction.getEngFlowEventData() != null));
										JsonNode readTree = helper.readableJson(activity);

										if (readTree != null) {
											helper.updateEventDataAndType(aggRequest.getEventId(),
													aggRequest.getEngFlowId(), readTree);

											Optional<EngFlowEvent> engFlowEventOp = engFlowEventService
													.getFlowEvent(aggRequest.getEventId());
											if (engFlowEventOp.isPresent()) {
												EngFlowEvent engFlowEvent = engFlowEventOp.get();

												engFlowEvent.setAutoConfigEventId(engFlowEvent.getEngFlowEventId());
												engFlowEvent.setEventProgress(100.00);
												engFlowEventService.save(engFlowEvent);
											}
										}
									}
								}
							}
						}
					} catch (Exception e) {
						log.error("Failed to initialize the dataframes associated with the Graph elements: {}",
								e.getMessage());
					}
				}
			} else {
				response = helper.failureMessage(200, "Empty - Zero records found!");
			}
		} catch (final SQLException e) {
			response = helper.failureMessage(400, e.getMessage());
			log.error("Error {} and {}", e.getErrorCode(), e.getMessage());
		} catch (final Exception ex) {
			response = helper.failureMessage(400, ex.getMessage());
			log.error("Error {}", ex.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	private HashMap<String, Object> getGraphContents(EngFlow engGraph) throws SQLException {
		Map<String, Object> contentResult = new HashMap<>();
		contentResult.put("graph", engGraph.getEngFlowMetaData());
		contentResult.put("config", engGraph.getEngFlowConfig());
		contentResult.put("projectId", engGraph.getProjectId());
		contentResult.put("engFlowId", engGraph.getEngFlowId());
		contentResult.put("engFlowName", engGraph.getEngFlowName());
		helper.getProjectAndChangeSchema(engGraph.getProjectId());
		List<EngFlowEvent> allEventsData = engFlowEventService.getAllEventsDataWithStatusTrue(true,
				engGraph.getEngFlowId());
		contentResult.put("events", allEventsData);
		return helper.createSuccessMessage(200, "Graph rendered successfully", contentResult);
	}

	@ApiOperation(value = "Save graph data", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Content saved"),
			@ApiResponse(code = 500, message = "Failed to store content") })
	@PostMapping(value = "/api/v1/graph", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> saveXmlContent(@RequestBody final EngGraphRequest engGraphRequest) {
		Map<String, Object> saveGraphResponse;
		try {
			log.info("Save content xml. Engineering Flow Id: {} and Project Id: {}", engGraphRequest.getEngFlowId(),
					engGraphRequest.getProjectId());
			Projects projects = helper.getProjects(engGraphRequest.getProjectId());
			EngFlow engFlow;
			Optional<EngFlow> enGraphDetails = engFlowService.getFlowsForProjectAndId(engGraphRequest.getProjectId(),
					engGraphRequest.getEngFlowId());
			if (enGraphDetails.isPresent()) {
				engFlow = enGraphDetails.get();
				engFlow.setEngFlowMetaData(engGraphRequest.getContent());
				engFlow.setEngFlowConfig(engGraphRequest.getConfig());
				engFlow.setModifiedDate(new Date());
				engFlow = engFlowService.save(engFlow);

				// Cleanup the DataFrames that are no longer needed. This will ensure the Livy
				// Session memory is properly managed...
				helper.cleanupLivySessionMemory(projects, engFlow.getEngFlowId());

				persistAllEvents(engGraphRequest.getEngFlowId());
				persistAllDeletedEvents(engGraphRequest.getEngFlowId());

				deleteAllNonTrueEvents(engGraphRequest.getEngFlowId());

				// Update the autorun_req_payload to null, if there are any auto run's
				// associated with this eng flow id.
				List<EngFlow> childEngFlows = engFlowService.getChildEngFlows(engGraphRequest.getProjectId(),
						engGraphRequest.getEngFlowId());
				if (childEngFlows != null && !childEngFlows.isEmpty()) {
					childEngFlows.stream().forEach((childEngFlow) -> {
						EngFlowJob flowJob = engFlowJobService.getJobByEngFlowId(childEngFlow.getEngFlowId());
						flowJob.setAutorunReqPayload(null);

						engFlowJobService.save(flowJob);
					});
				}

				saveGraphResponse = helper.createSuccessMessage(201, "Flow saved successfully.",
						engFlow.getEngFlowId());
			} else {
				saveGraphResponse = helper.failureMessage(404, "Please create EngFlow before saving contents");
			}
		} catch (final SQLException e) {
			saveGraphResponse = helper.failureMessage(500, e.getMessage());
			log.error("Error {} and {}", e.getErrorCode(), e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(saveGraphResponse);
	}

	private void persistAllEvents(int engFlowId) throws SQLException {
		engFlowEventService.persistEventStatus(engFlowId, true);
	}

	private void persistAllDeletedEvents(int engFlowId) throws SQLException {
		engFlowEventService.persistDeletedEventStatus(engFlowId, false);
	}

	private void deleteAllNonTrueEvents(int engFlowId) throws SQLException {
		engFlowEventService.deleteEventsByStatus(engFlowId, false);
	}

	@ApiOperation(value = "Delete graph data details", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 500, message = "Failed delete content") })
	@DeleteMapping("/api/v1/graph/{projectId}/{engGraphId}")
	public ResponseEntity<Object> deleteXmlContent(@PathVariable(value = "projectId") int projectId,
			@PathVariable(value = "engGraphId") int engGraphId) {
		Map<String, Object> deleteGraphResponse;
		try {
			helper.getProjectAndChangeSchema(projectId);
			engGraphService.deleteGraph(engGraphId);
			deleteGraphResponse = helper.createSuccessMessage(204, "NoContent", "");
		} catch (final SQLException e) {
			deleteGraphResponse = helper.failureMessage(500, e.getMessage());
			log.error("Error {} and {}", e.getErrorCode(), e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(deleteGraphResponse);
	}

	@ApiOperation(value = "Delete Event Record EngFlowEventId", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 500, message = "Failed to delete.") })
	@PutMapping("/api/v1/event/{projectId}/{engFlowEventId}")
	public ResponseEntity<Object> deleteEngFlowEvent(@PathVariable(value = "projectId") int projectId,
			@PathVariable(value = "engFlowEventId") int engFlowEventId) {
		Map<String, Object> deleteEventResponse;
		try {
			helper.getProjectAndChangeSchema(projectId);

			Optional<EngFlowEvent> engFlowEventOp = engFlowEventService.getFlowEvent(engFlowEventId);
			if (engFlowEventOp.isPresent()) {
				EngFlowEvent engFlowEvent = engFlowEventOp.get();

				engFlowEvent.setDeleted(true);
				engFlowEventService.save(engFlowEvent);
			}

			deleteEventResponse = helper.createSuccessMessage(204, "Deleted successfully.", "");
		} catch (final SQLException e) {
			deleteEventResponse = helper.failureMessage(500, e.getMessage());
			log.error("Error {} and {}", e.getErrorCode(), e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(deleteEventResponse);
	}

	@ApiOperation(value = "Get All Engineering Flows", response = Json.class, notes = "Get All Engineering flow")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Engineering Flows Found!"),
			@ApiResponse(code = 404, message = "Not Found!") })
	@GetMapping(value = "/api/v1/engFlowActive/{userId}/{projectId}")
	public ResponseEntity<Object> getAllEngFlows(@PathVariable(value = "userId") final int userId,
			@PathVariable(value = "projectId") final int projectId) {
		Map<String, Object> response;
		try {
			Projects projects = helper.getProjects(projectId);
			helper.getProjectAndChangeSchema(projectId);
			List<EngineeredFilesRedashResponse> flows = getCompletedEngFlowsInProject(projects, userId, false);
			response = helper.createSuccessMessage(200, "Flow Objects", flows);
		} catch (Exception exception) {
			response = helper.failureMessage(500, exception.getMessage());
			log.error("Exception reading Eng Flows {}", exception.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
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

	@ApiOperation(value = "Get Parent Engineering Flows", response = Json.class, notes = "Get Parent Engineering flow")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Engineering Flows Found!"),
			@ApiResponse(code = 204, message = "Not Found!") })
	@GetMapping(value = "/api/v1/flow/{userId}/{projectId}")
	public ResponseEntity<Object> getParentFlowsOnly(@PathVariable(value = "userId") final int userId,
			@PathVariable(value = "projectId") final int projectId) {
		Map<String, Object> response;
		try {
			helper.getProjectAndChangeSchema(projectId);
			List<EngFlow> flows = engFlowService.getFlowNames(projectId, userId);
			List<EngFlow> collect = new ArrayList<>();
			for (EngFlow engFlow : flows) {
				if (engFlow.getParentEngFlowId() == 0) {
					engFlow.setEngFlowMetaData("");
					engFlow.setEngFlowConfig("");

					collect.add(engFlow);
				}
			}

			if (collect != null && !collect.isEmpty()) {
				Collections.sort(collect, new Comparator<EngFlow>() {
					@Override
					public int compare(EngFlow o1, EngFlow o2) {
						Integer i1 = Integer.valueOf(o1.getEngFlowId());
						Integer i2 = Integer.valueOf(o2.getEngFlowId());

						return i2.compareTo(i1);
					}
				});
			}

			response = helper.createSuccessMessage(200, "Flow Objects", collect);
		} catch (SQLException exception) {
			response = helper.failureMessage(500, exception.getMessage());
			log.error("Exception reading Eng Flows {}", exception.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@ApiOperation(value = "Save engineering flow", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Engineering flow saved!"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 422, message = "Unprocessable Entity"),
			@ApiResponse(code = 409, message = "Duplicate Engineering flow name") })
	@PostMapping(value = "/api/v1/flow")
	public ResponseEntity<Object> saveEngFlow(@RequestBody @Valid EngFlowRequest engFlow) {
		Map<String, Object> response;
		try {
			String schemaName = helper.getProjectAndChangeSchema(engFlow.getProjectId());
			String userName = helper.getUserName(engFlow.getUserId(), schemaName);
			EngFlow engFlowModel = convertIntoEngFlow(engFlow);
			if (engFlowService.isExists(engFlowModel.getEngFlowName())) {
				response = helper.createSuccessMessage(409,
						"Engineering Flow with name '" + engFlow.getEngFlowName() + "' already exists.",
						"Engineering Flow with name '" + engFlow.getEngFlowName() + "' already exists.");
			} else {
				engFlowModel.setCreatedBy(userName);
				engFlowModel = engFlowService.save(engFlowModel);
				String auditMessage = helper.getAuditMessage(AuditEventType.ENGINEER, AuditEventTypeAction.CREATE);
				helper.recordAuditEvent(AuditEventType.ENGINEER, AuditEventTypeAction.CREATE,
						String.format(auditMessage, engFlow.getEngFlowName()), null, engFlow.getUserId(),
						engFlow.getProjectId());
				response = helper.createSuccessMessage(200, "Flow saved successfully!", engFlowModel);
			}
		} catch (SQLException exception) {
			response = helper.failureMessage(404, exception.getMessage());
			log.error("Exception reading Eng Flows {}", exception.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	private EngFlow convertIntoEngFlow(EngFlowRequest engFlow) {
		EngFlow engFlowObject = new EngFlow();
		engFlowObject.setActive(true);
		engFlowObject.setProjectId(engFlow.getProjectId());
		engFlowObject.setUserId(engFlow.getUserId());
		engFlowObject.setEngFlowConfig(engFlow.getEngFlowConfig());
		engFlowObject.setEngFlowDesc(engFlow.getEngFlowDesc());
		engFlowObject.setEngFlowMetaData(engFlow.getEngFlowMetaData());
		engFlowObject
				.setEngFlowName(engFlow.getEngFlowName().trim().replaceAll(QsConstants.EMPTY, QsConstants.UNDERSCORE));
		engFlowObject.setParentEngFlowId(engFlow.getParentEngFlowId());
		engFlowObject.setCreatedDate(new Date());
		engFlowObject.setEngFlowDisplayName(engFlow.getEngFlowName());
		return engFlowObject;
	}

	@PutMapping(value = "/api/v1/flow/{projectId}/{userId}/{engFlowId}")
	public ResponseEntity<Object> deleteEngFlow(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "engFlowId") final int engFlowId) {
		Map<String, Object> response = new HashMap<>();
		try {
			log.info(" {} - {}", projectId, engFlowId);
			helper.getProjects(projectId);

			Optional<EngFlow> engFlowOp = engFlowService.getFlowsForProjectAndId(projectId, engFlowId);
			EngFlow engFlow = null;
			if (engFlowOp.isPresent()) {
				engFlow = engFlowOp.get();

				// Update the Active flag...
				engFlow.setActive(false);
				engFlow = engFlowService.save(engFlow);
				String auditMessage = helper.getAuditMessage(AuditEventType.ENGINEER, AuditEventTypeAction.DELETE);
				String notificationMessage = helper.getNotificationMessage(AuditEventType.ENGINEER,
						AuditEventTypeAction.DELETE);
				String userName = helper.getUserName(userId);
				helper.recordAuditEvent(AuditEventType.ENGINEER, AuditEventTypeAction.DELETE,
						String.format(auditMessage, engFlow.getEngFlowName()),
						String.format(notificationMessage, engFlow.getEngFlowName(), userName), userId,
						engFlow.getProjectId());
				response = helper.createSuccessMessage(HttpStatus.OK.value(), "Eng Flow Deleted!", engFlowId);
			} else {
				response = helper.createSuccessMessage(HttpStatus.BAD_REQUEST.value(), "Eng Flow not found!",
						engFlowId);
			}

		} catch (SQLException exception) {
			log.info(" Exception {}", exception.getMessage());
			response = helper.failureMessage(SC_INTERNAL_SERVER_ERROR, "Internal Error!");
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping(value = "/api/v1/flow/updateEngFlowName")
	public ResponseEntity<Object> updateEngFlowName(@Valid @RequestBody EngFlowInfo engFlowId) throws SQLException {
		Map<String, Object> response;
		log.info(" {} - {}", engFlowId.getProjectId(), engFlowId.getEngFlowId());
		helper.getProjects(engFlowId.getProjectId());
		String engFlowName = engFlowId.getEngFlowName().trim().replaceAll(QsConstants.EMPTY, QsConstants.UNDERSCORE);
		List<EngFlow> engFlowList = engFlowService.getFlowsForProjectAndFlowName(engFlowId.getProjectId(), engFlowName);

		if (engFlowList.size() > 0 && engFlowId.getEngFlowId() != engFlowList.get(0).getEngFlowId()) {
			response = helper.createSuccessMessage(409,
					"Engineering Flow with name '" + engFlowName + "' already exists.",
					"Engineering Flow with name '" + engFlowName + "' already exists.");
		} else {
			response = engFlowService.getFlowsForProjectAndId(engFlowId.getProjectId(), engFlowId.getEngFlowId())
					.map(engFlowUpdate -> {
						try {
							engFlowUpdate.setEngFlowDisplayName(engFlowId.getEngFlowName());
							engFlowUpdate.setEngFlowName(engFlowId.getEngFlowName().trim().replaceAll(QsConstants.EMPTY,
									QsConstants.UNDERSCORE));
							engFlowUpdate.setEngFlowDesc(engFlowId.getEngFlowDesc());

							EngFlow engFlow = engFlowService.save(engFlowUpdate);

							String auditMessage = helper.getAuditMessage(AuditEventType.ENGINEER,
									AuditEventTypeAction.UPDATE);
							String notificationMessage = helper.getNotificationMessage(AuditEventType.ENGINEER,
									AuditEventTypeAction.UPDATE);
							String userName = helper.getUserName(engFlowId.getUserId());
							helper.recordAuditEvent(AuditEventType.ENGINEER, AuditEventTypeAction.UPDATE,
									String.format(auditMessage, engFlow.getEngFlowDisplayName()),
									String.format(notificationMessage, engFlow.getEngFlowDisplayName(), userName),
									engFlowId.getUserId(), engFlow.getProjectId());
							return helper.createSuccessMessage(HttpStatus.OK.value(), "Eng Flow name Updated!",
									engFlowId);
						} catch (Exception exception) {
							log.info(" Exception {}", exception);
							return helper.failureMessage(SC_INTERNAL_SERVER_ERROR, "Internal Error!");
						}
					}).orElseGet(() -> helper.createSuccessMessage(HttpStatus.BAD_REQUEST.value(),
							"Eng Flow not found!", engFlowId));
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("/api/v1/ops/file")
	public ResponseEntity<Object> livyFileOperation(@RequestBody DataFrameRequest wsRequest) {
		log.info("FIle input :: {}", wsRequest.toString());

		HashMap<String, Object> eventResponse;
		try {
			if (wsRequest.getProjectId() > 0 && wsRequest.getFolderId() > 0 && wsRequest.getFileId() > 0) {
				Projects project = helper.getProjects(wsRequest.getProjectId());
				EngFlowEvent flowEvent = helper.getMeEngFlowEvent(wsRequest.getEventId());

				FileJoinColumnMetadata fileJoinColMetadata = gson.fromJson(flowEvent.getFileMetaData(),
						FileJoinColumnMetadata.class);
				log.info("File Metadata is: {}", fileJoinColMetadata);

				wsRequest.setMetadata(fileJoinColMetadata);
				wsRequest.setAutoConfigEventId(flowEvent.getAutoConfigEventId());

				log.info("Updated File input :: {}", wsRequest.toString());

				helper.updateFlowEventWithData(wsRequest, flowEvent, project,
						(flowEvent.getEngFlowEventData() != null && !flowEvent.getEngFlowEventData().isEmpty()));
				eventResponse = helper.createSuccessMessage(200, "Action saved!", flowEvent.getEngFlowEventId());
			} else {
				eventResponse = helper.failureMessage(400, "BadRequest !");
				log.error("Attributes cant not be zero!");
			}
		} catch (Exception exception) {
			eventResponse = helper.failureMessage(500, "Internal Server Error !" + exception.getMessage());
			log.error("Exception - {}", exception.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(eventResponse);
	}

	@PostMapping("/api/v1/ops/join")
	public ResponseEntity<Object> livyJoinOperation(@RequestBody @Valid JoinOperationRequest wsJoinRequest) {
		log.info("Join Operation requested for {}", wsJoinRequest.toString());
		HashMap<String, Object> joinResponse;
		try {
			EngFlowEvent joinAction;
			helper.getProjectAndChangeSchema(wsJoinRequest.getProjectId());
			Optional<EngFlowEvent> engFlowEventRecord = engFlowEventService.getFlowEvent(wsJoinRequest.getEventId());
			if (engFlowEventRecord.isPresent()) {
				joinAction = engFlowEventRecord.get();
				helper.updateFlowEventWithJoinResults(wsJoinRequest, joinAction);
				log.info("Performing join operation :: for event {} and for flow {}", joinAction.getEngFlowEventId(),
						joinAction.getEngFlowId());

				joinResponse = helper.createSuccessMessage(200, "Join Action Event captured",
						joinAction.getEngFlowEventId());
			} else {
				joinResponse = helper.failureMessage(200, "No Records Found!");
			}

		} catch (final Exception e) {
			log.error("Exception while performing join operation :: {}", e.getMessage());
			joinResponse = helper.failureMessage(500, "Internal Server error " + e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(joinResponse);
	}

	@PostMapping("/api/v1/ops/udf")
	public ResponseEntity<Object> livyUdfOperation(@RequestBody @Valid UdfOperationRequest wsUdfRequest) {
		log.info("Udf Operation requested for {}", wsUdfRequest.toString());
		HashMap<String, Object> udfResponse;
		try {
			EngFlowEvent udfAction;
			helper.getProjectAndChangeSchema(wsUdfRequest.getProjectId());
			Map<String, Integer> eventIds = wsUdfRequest.getEventIds();
			int eventId = eventIds.get("eventId");
			Optional<EngFlowEvent> engFlowEventRecord = engFlowEventService.getFlowEvent(eventId);
			if (engFlowEventRecord.isPresent()) {
				udfAction = engFlowEventRecord.get();
				helper.updateFlowEventWithUdfResults(wsUdfRequest, udfAction);
				log.info("Performing udf operation :: for event {} and for flow {}", udfAction.getEngFlowEventId(),
						udfAction.getEngFlowId());
				udfResponse = helper.createSuccessMessage(200, "Udf Action Event captured",
						udfAction.getEngFlowEventId());
			} else {
				udfResponse = helper.failureMessage(200, "No Records Found!");
			}
		} catch (final Exception e) {
			log.error("Exception while performing udf operation :: {}", e.getMessage());
			udfResponse = helper.failureMessage(500, "Internal Server error " + e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(udfResponse);
	}

	@ApiOperation(value = "Get files list to trigger the Autorun.", response = Json.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully returned list of files to trigger autorun"),
			@ApiResponse(code = 400, message = "Engineering flow not found") })
	@GetMapping(value = "/api/v1/ops/selectfilestorun/{projectId}/{userId}/{engFlowId}")
	public ResponseEntity<Object> getFilesToAutorun(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "engFlowId") final int engFlowId) {
		Map<String, Object> response = new HashMap<>();
		try {
			Projects project = helper.getProjects(projectId, userId);
			if (project == null) {
				response.put("code", HttpStatus.BAD_REQUEST);
				response.put("message",
						"Requested project with Id: " + projectId + " for User with Id: " + userId + " not found.");

				return ResponseEntity.ok().body(response);
			}

			List<String> types = new ArrayList<>();
			types.add(QsConstants.FILE_OP);
			types.add(QsConstants.ENG_OP);

			deleteAllNonTrueEvents(engFlowId);
			List<EngFlowEvent> events = engFlowEventService.getAllEventsOfType(engFlowId, types);
			if (events != null && !events.isEmpty()) {
				Map<String, List<AutoRunFileInfo>> filesInfoMap = new HashMap<>();
				List<AutoRunFileInfo> rawFilesInfo = new ArrayList<>();
				List<AutoRunFileInfo> cleansedFilesInfo = new ArrayList<>();
				List<AutoRunFileInfo> engFilesInfo = new ArrayList<>();

				Set<String> rawIds = new HashSet<>();
				Set<String> cleansedIds = new HashSet<>();

				for (EngFlowEvent event : events) {
					if (QsConstants.ENG_OP.equals(event.getFileType())) {
						Optional<EngFlow> engFlowOp = engFlowService.getFlowsForProjectAndId(projectId,
								event.getFileId());
						EngFlow engFlow = null;
						if (engFlowOp.isPresent()) {
							engFlow = engFlowOp.get();
						}

						List<EngFlowEvent> engFlowEvents = engFlowEventService.getFlowEventForFileId(engFlowId,
								event.getFolderId(), event.getFileId(), QsConstants.ENG_OP);
						EngFlowEvent engFlowEvent = null;
						if (engFlowEvents != null && !engFlowEvents.isEmpty()) {
							engFlowEvent = engFlowEvents.get(0);
						}

						AutoRunFileInfo arfie = new AutoRunFileInfo();
						arfie.setFileId(engFlow.getEngFlowId());
						arfie.setEventId(
								(engFlowEvent != null) ? engFlowEvent.getEngFlowEventId() : event.getEngFlowEventId());
						arfie.setEngFlowId(engFlow.getEngFlowId());
						arfie.setEngFlowName(engFlow.getEngFlowName());
						arfie.setFlowCreationDate(engFlow.getCreatedDate());
						engFilesInfo.add(arfie);

					} else {
						QsFolders folder = folderService.getFolder(event.getFolderId());
						if (QsConstants.RAW.equals(event.getFileType())) {
							List<QsFiles> rawFiles = fileService.getFiles(projectId, folder.getFolderId());
							if (rawFiles != null && !rawFiles.isEmpty()) {
								int rawFileId = -1;
								for (QsFiles rawFile : rawFiles) {
									rawFileId = rawFile.getFileId();
									String rawKey = folder.getFolderId() + "_" + rawFileId;
									if (!rawIds.add(rawKey)) {
										// This folder is already covered, hence no need to cover it again..
										continue;
									}

									AutoRunFileInfo arfi = new AutoRunFileInfo();
									List<EngFlowEvent> engFlowEvents = engFlowEventService.getFlowEventForFileId(
											engFlowId, folder.getFolderId(), rawFile.getFileId(), QsConstants.RAW);
									EngFlowEvent engFlowEvent = null;
									if (engFlowEvents != null && !engFlowEvents.isEmpty()) {
										engFlowEvent = engFlowEvents.get(0);
									}

									arfi.setEventId((engFlowEvent != null) ? engFlowEvent.getEngFlowEventId()
											: event.getEngFlowEventId());
									arfi.setFolderId(folder.getFolderId());
									arfi.setFolderName(folder.getFolderName());
									arfi.setFolderDisplayName(
											(folder.getFolderDisplayName() == null) ? folder.getFolderName()
													: folder.getFolderDisplayName());
									arfi.setFileId(rawFile.getFileId());
									arfi.setFileName(rawFile.getFileName());
									arfi.setCreationDate(rawFile.getCreatedDate());

									rawFilesInfo.add(arfi);
								}
							}
						} else if (QsConstants.PROCESSED.equals(event.getFileType())) {
							List<RunJobStatus> cleanseJobs = runJobService.getSucceededJobsInFolder(userId, projectId,
									event.getFolderId());

							if (cleanseJobs != null && !cleanseJobs.isEmpty()) {
								int cleansedFileId = -1;
								for (RunJobStatus runJob : cleanseJobs) {
									cleansedFileId = runJob.getFileId();
									String key = folder.getFolderId() + "_" + cleansedFileId;
									if (!cleansedIds.add(key)) {
										// This folder and file combination is already covered, hence no need to cover
										// it again..
										continue;
									}

									Optional<QsFiles> fileOp = fileService.getFileById(cleansedFileId);
									QsFiles file = null;
									if (fileOp.isPresent()) {
										file = fileOp.get();

										List<EngFlowEvent> engFlowEvents = engFlowEventService.getFlowEventForFileId(
												engFlowId, file.getFolderId(), file.getFileId(), QsConstants.PROCESSED);
										EngFlowEvent engFlowEvent = null;
										if (engFlowEvents != null && !engFlowEvents.isEmpty()) {
											engFlowEvent = engFlowEvents.get(0);
										}

										AutoRunFileInfo arfic = new AutoRunFileInfo();
										arfic.setEventId((engFlowEvent != null) ? engFlowEvent.getEngFlowEventId()
												: event.getEngFlowEventId());
										arfic.setFolderId(file.getFolderId());
										arfic.setFolderName(folder.getFolderName());
										arfic.setFolderDisplayName(
												(folder.getFolderDisplayName() == null) ? folder.getFolderName()
														: folder.getFolderDisplayName());
										arfic.setFileId(file.getFileId());
										arfic.setFileName(file.getFileName());
										arfic.setCreationDate(file.getCreatedDate());

										cleansedFilesInfo.add(arfic);
									}
								}
							}
						}
					}
				}

				filesInfoMap.put(QsConstants.RAW, rawFilesInfo);
				filesInfoMap.put(QsConstants.PROCESSED, cleansedFilesInfo);
				filesInfoMap.put(QsConstants.ENG_OP, engFilesInfo);

				response = helper.createSuccessMessage(200, "Successfully returned list of files to trigger autorun",
						filesInfoMap);
			} else {
				response = helper.createSuccessMessage(400, "No events found for the Engineering Flow Id: " + engFlowId,
						Collections.emptyMap());
			}
		} catch (final SQLException e) {
			response = helper.failureMessage(404, e.getMessage());
			log.error("Error {} and {}", e.getErrorCode(), e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("/api/v1/ops/qswsrun")
	public ResponseEntity<Object> runEngineeringFlow(@RequestBody EngRunFlowRequest runFlowRequest) {
		log.info("Engineering Operation requested for {}", runFlowRequest.toString());

		HashMap<String, Object> engJobExeResponse = new HashMap<>();
		int jobId = -1;
		int batchJobId = -1;
		List<String> udfFilePath = new LinkedList<>();
		try {
			Projects projects = helper.getProjects(runFlowRequest.getProjectId());

			// Get the Engineering Flow Config column value using the EngFlowId and parse
			// that information
			Optional<EngFlow> content = engFlowService.getFlowsForProjectAndId(runFlowRequest.getProjectId(),
					runFlowRequest.getEngFlowId());

			if (content.isPresent()) {
				EngFlow engFlow = content.get();
				String engFlowConfigJson = engFlow.getEngFlowConfig();

				/*
				 * Replace the engFlowId with the current engFlowId that is available in the
				 * Autorun request...
				 */
				engFlowConfigJson = engFlowConfigJson.replaceAll("\"engFlowId\":[0-9]*",
						"\"engFlowId\":" + runFlowRequest.getEngFlowId());

				/*
				 * Process the configuration JSON and prepare the information in the
				 * DataFrameRequest format for further processing...
				 */
				EngFlowConfigRequest engFlowConfigRequest = gson.fromJson(engFlowConfigJson,
						EngFlowConfigRequest.class);

				int finalEventId = -1;
				log.info("Engineering flow config payload used for processing is: {}", engFlowConfigJson);

				StringBuilder fileContents = new StringBuilder();
				List<DataFrameRequest> selectedFiles = engFlowConfigRequest.getSelectedFiles();
				if (selectedFiles != null && !selectedFiles.isEmpty()) {
					StringBuilder temp = new StringBuilder();
					helper.readLinesFromTemplate(temp);

					fileContents = fileContents.append(temp.toString().replace(SERVICE_SERVER_URL_PREFIX,
							String.format("'%s'", serviceServerHostUrl)));
					jobId = runFlowRequest.getFlowJobId();

					finalEventId = helper.runEngJobFileOpHandler(engFlowConfigRequest.getSelectedFiles(), projects,
							runFlowRequest.getEngFlowId(), runFlowRequest.getFlowJobId(), fileContents);
				}

				List<JoinOperationRequest> selectedJoins = engFlowConfigRequest.getSelectedJoins();
				if (selectedJoins != null && !selectedJoins.isEmpty()) {
					finalEventId = helper.runEngJobJoinOpHandler(engFlowConfigRequest.getSelectedJoins(), projects,
							runFlowRequest.getEngFlowId(), jobId, fileContents);
				}

				List<UdfOperationRequest> selectedUdf = engFlowConfigRequest.getSelectedUdfs();
				if (selectedUdf != null && !selectedUdf.isEmpty()) {
					finalEventId = helper.runEngJobUdfOpHandler(engFlowConfigRequest.getSelectedUdfs(), projects,
							runFlowRequest.getEngFlowId(), jobId, fileContents);
				}

				for (UdfOperationRequest udfReq : selectedUdf) {
					if (udfReq != null) {
						QsUdf qsUdf = udfService.getByUdfIdAndProjectIdAndUserId(udfReq.getFileId(),
								projects.getProjectId(), projects.getUserId());
						if (qsUdf != null) {
							String path = String.format("s3://%s/%s/%s", qsUdfBucket, projects.getProjectId(),
									qsUdf.getUdfName() + ".py");
							if (!udfFilePath.contains(path)) {
								udfFilePath.add(path);
							}
						}
					}
				}

				List<AggregateRequest> aggregates = engFlowConfigRequest.getSelectedAggregates();
				if (aggregates != null && !aggregates.isEmpty()) {
					finalEventId = helper.runEngJobAggOpHandler(engFlowConfigRequest.getSelectedAggregates(), projects,
							runFlowRequest.getEngFlowId(), jobId, fileContents);
				}

				log.info("Final Event received is: {}", finalEventId);
				EngFinalRequest engFinalRequest = new EngFinalRequest(finalEventId, runFlowRequest.getProjectId(),
						jobId, null);

				helper.getProjectAndChangeSchema(runFlowRequest.getProjectId());

				Optional<EngFlowEvent> dbEventOp = engFlowEventService
						.getFlowEventForEngFlowAndType(engFlow.getParentEngFlowId(), QsConstants.DB_OP);
				boolean saveFinalEventDatatoDb = false;
				if (dbEventOp.isPresent()) {
					saveFinalEventDatatoDb = true;
				}

				BatchJobInfo batchJobInfo = engHelper.saveEngResult(engFinalRequest, saveFinalEventDatatoDb,
						fileContents, engFlow.getEngFlowId(), engFlowConfigRequest, udfFilePath);

				// Insert into the Data Lineage table for further processing..
				helper.insertDataLineageInfo(engFlow.getEngFlowId(), engFlowConfigRequest);

				String jobStatus = batchJobInfo.getBatchJobStatus();
				batchJobId = batchJobInfo.getBatchJobId();
				if (JobRunState.SUCCEEDED.toString().equals(jobStatus)) {
					helper.updateJobTableStatus(jobId, JobRunState.SUCCEEDED.toString(), batchJobId,
							batchJobInfo.getBatchJobLog(), batchJobInfo.getBatchJobDuration());

					// Get the parent Engineering flow Config and update the config JSON to child as
					// per the requirement related
					// to Get Graph and Preview All flows in the UI.

					Optional<EngFlow> parentEngFlowOp = engFlowService.getFlowsForProjectAndId(projects.getProjectId(),
							engFlow.getParentEngFlowId());
					if (parentEngFlowOp.isPresent()) {
						EngFlow parentEngFlow = parentEngFlowOp.get();

						/* Save the updated Autorun Run Config JSON to QS_ENG_FLOW table.. */
						engFlow.setEngFlowConfig(parentEngFlow.getEngFlowConfig());
						engFlow = engFlowService.save(engFlow);
					}
				} else {
					helper.updateJobTableStatus(jobId, JobRunState.FAILED.toString(), batchJobId,
							batchJobInfo.getBatchJobLog(), batchJobInfo.getBatchJobDuration());
				}

				engJobExeResponse = helper.createSuccessMessage(200, "Engineering Job executed!!!",
						runFlowRequest.getEngFlowId());

			} else {
				log.info("Error occurred while processing the selected files.");
			}

		} catch (Exception exception) {
			helper.updateJobTableStatus(jobId, JobRunState.FAILED.toString(), batchJobId, exception.getMessage(), -1);
			log.error("Message -- {}", exception.getMessage());
			engJobExeResponse = helper.createSuccessMessage(400,
					"Exception occurred during Engineering Job execution: " + exception.getMessage(),
					runFlowRequest.getEngFlowId());
		}

		return ResponseEntity.status(HttpStatus.OK).body(engJobExeResponse);
	}

	@ApiOperation(value = "perform aggregate", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Engineering flow saved!"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@PostMapping("/api/v1/aggregate")
	public ResponseEntity<Object> runAggregate(@RequestBody AggregateRequest aggregateRequest) {
		HashMap<String, Object> aggResponse = null;
		try {
			Projects projects = helper.getProjects(aggregateRequest.getProjectId());
			Optional<EngFlowEvent> engFlowEventOp = engFlowEventService.getFlowEvent(aggregateRequest.getEventId());

			EngFlowEvent engFlowEvent = null;
			if (engFlowEventOp.isPresent()) {
				engFlowEvent = engFlowEventOp.get();
			}

			JsonElement activity = helper.getActivity(aggregateRequest, null,
					(engFlowEvent.getEngFlowEventData() != null));
			JsonNode readTree = helper.readableJson(activity);

			engFlowEvent = helper.updateEventDataAndType(aggregateRequest.getEventId(), aggregateRequest.getEngFlowId(),
					readTree);

			engFlowEvent.setAutoConfigEventId(engFlowEvent.getEngFlowEventId());
			engFlowEvent.setEventProgress(100.00);
			engFlowEventService.save(engFlowEvent);

			aggResponse = helper.createSuccessMessage(200, "Aggregation performed", readTree);
		} catch (Exception e) {
			e.printStackTrace();
			aggResponse = helper.failureMessage(500, "INTERNAL SERVER ERROR");
		}
		return ResponseEntity.status(HttpStatus.OK).body(aggResponse);
	}

	@PostMapping("/api/v1/ops/eng")
	public ResponseEntity<Object> livyEngFileOperation(@RequestBody EngFileOperationRequest wsRequest) {
		log.info("FIle input :: {}", wsRequest.toString());

		HashMap<String, Object> eventResponse;
		try {
			if (wsRequest.getProjectId() > 0) {
				Projects project = helper.getProjects(wsRequest.getProjectId());
				EngFlowEvent flowEvent = helper.getMeEngFlowEvent(wsRequest.getEventId());
				EngFlowMetaDataAwsRef engFlowMetaDataAwsRef = metadataHelper
						.getLatestCompletedEngFlowsInfo(wsRequest.getEngFlowName());

				log.info("EngFlowMetadataAwsRef instance: {}", engFlowMetaDataAwsRef);
				String fileMetadata = metadataHelper.getFileMetadata(engFlowMetaDataAwsRef.getEngFlowMetaData(),
						flowEvent.getEngFlowEventId());
				log.info("Metadata is: {}", fileMetadata);

				flowEvent.setFileMetaData(fileMetadata);
				engFlowEventService.save(flowEvent); // Save the event after setting the Eng File metadata

				FileJoinColumnMetadata fileJoinColMetadata = gson.fromJson(flowEvent.getFileMetaData(),
						FileJoinColumnMetadata.class);
				log.info("File Metadata is: {}", fileJoinColMetadata);

				wsRequest.setMetadata(fileJoinColMetadata);
				wsRequest.setAutoConfigEventId(flowEvent.getAutoConfigEventId());

				log.info("Updated File input :: {}", wsRequest.toString());

				helper.updateEngFileEventWithData(wsRequest, flowEvent, project, wsRequest.getEngFlowId());
				eventResponse = helper.createSuccessMessage(200, "Action saved!", flowEvent.getEngFlowEventId());
			} else {
				eventResponse = helper.failureMessage(400, "BadRequest !");
				log.error("Attributes cant not be zero!");
			}
		} catch (Exception exception) {
			eventResponse = helper.failureMessage(500, "Internal Server Error !" + exception.getMessage());
			log.error("Exception - {}", exception.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(eventResponse);
	}

	@ApiOperation(value = "Save final output of the engineering screen", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Engineering result saved!"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@PostMapping("/api/v1/opfinal")
	public ResponseEntity<Object> saveEngResult(@RequestBody EngFinalRequest engFinalRequest) {
		HashMap<String, Object> opFinalResp = null;
		try {

			Optional<EngFlowEvent> eventOp = engFlowEventService.getFlowEvent(engFinalRequest.getEventId1());
			boolean saveFinalEventDatatoDb = false;
			int engFlowId = -1;
			if (eventOp.isPresent()) {
				EngFlowEvent event = eventOp.get();
				engFlowId = event.getEngFlowId();
				Optional<EngFlowEvent> dbEventOp = engFlowEventService.getFlowEventForEngFlowAndType(engFlowId,
						QsConstants.DB_OP);
				if (dbEventOp.isPresent()) {
					saveFinalEventDatatoDb = true;
				}
			}

			engHelper.saveEngResult(engFinalRequest, saveFinalEventDatatoDb, null, engFlowId, null, null);
			opFinalResp = helper.createSuccessMessage(200, "Result saved", "");
		} catch (Exception e) {
			opFinalResp = helper.failureMessage(500, "INTERNAL SERVER ERROR");
		}
		return ResponseEntity.status(HttpStatus.OK).body(opFinalResp);
	}

	@ApiOperation(value = "Get Engineering Flow Events status", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully returned Eng Flow Events status"),
			@ApiResponse(code = 400, message = "Engineering flow not found") })
	@GetMapping(value = "/api/v1/ops/{projectId}/{engFlowId}")
	public ResponseEntity<Object> getEngFlowEventsStatus(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "engFlowId") final int engFlowId) {
		Map<String, Object> response;
		try {
			helper.getProjectAndChangeSchema(projectId);
			List<EngFlowEvent> events = engFlowEventService.getAllEventsData(engFlowId);
			if (events != null && !events.isEmpty()) {
				Map<Integer, Double> eventsProgress = new HashMap<>();
				events.stream().forEach((event) -> {
					eventsProgress.put(event.getEngFlowEventId(), event.getEventProgress());
				});

				response = helper.createSuccessMessage(200, "Successfully returned Eng Flow Events status",
						eventsProgress);
			} else {
				response = helper.createSuccessMessage(400, "No events found for the Engineering Flow Id: " + engFlowId,
						Collections.emptyMap());
			}
		} catch (final SQLException e) {
			response = helper.failureMessage(404, e.getMessage());
			log.error("Error {} and {}", e.getErrorCode(), e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@ApiOperation(value = "Update the Eng Flow Event with data.", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Updated eng flow event data successfully."),
			@ApiResponse(code = 400, message = "Bad Request") })
	@PostMapping("/api/v1/ops/engflowevent")
	public ResponseEntity<Object> updateEngFlowEventData(@RequestBody EngFinalRequest engFinalRequest) {
		HashMap<String, Object> opFinalResp = null;
		try {

			log.info("Received EngFlowEvent data update request. Event Id: {}, Project Id: {}, ",
					engFinalRequest.getEventId1(), engFinalRequest.getProjectId());
			helper.getProjectAndChangeSchema(engFinalRequest.getProjectId());

			Optional<EngFlowEvent> eventOp = engFlowEventService.getFlowEvent(engFinalRequest.getEventId1());
			if (eventOp.isPresent()) {
				EngFlowEvent event = eventOp.get();
				event.setEngFlowEventData(engFinalRequest.getEventData());

				event = engFlowEventService.save(event);
				opFinalResp = helper.createSuccessMessage(200, "Updated Eng Flow Event data successfully.", "");
			} else {
				opFinalResp = helper.createSuccessMessage(200, "Eng Flow Event not found.", "");
			}

		} catch (Exception e) {
			opFinalResp = helper.failureMessage(500, "INTERNAL SERVER ERROR");
		}
		return ResponseEntity.status(HttpStatus.OK).body(opFinalResp);
	}

	@ApiOperation(value = "Destroy Livy Session after use.", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 500, message = "Failed to destroy the livy session.") })
	@DeleteMapping("/api/v1/ops/{userId}")
	public ResponseEntity<Object> destroyLivySession(@PathVariable(value = "userId") int userId) {
		Map<String, Object> response;
		try {
			String sessionName = "project0" + userId;
			// helper.deleteLivySession(sessionName);
			response = helper.createSuccessMessage(204, "Livy session successfully deleted.",
					"Livy session successfully deleted.");
		} catch (final Exception e) {
			response = helper.failureMessage(500, e.getMessage());
			log.error("Error: {} and Message is: {}", e.getCause(), e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@ApiOperation(value = "Fetch data lineage information for an Engineering flow", response = Json.class)
	@GetMapping(value = "/api/v1/ops/fetchdatalineageinfo/{projectId}/{userId}/{engFlowId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Fetched data lineage information successfully."),
			@ApiResponse(code = 400, message = "Failed to fetch the data lineage information.") })
	public ResponseEntity<Object> getDataLineageInformation(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "engFlowId") final int engFlowId) {
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("Received file upload request on: {}", QsConstants.getCurrentUtcDate());

			final Projects project = helper.getProjects(projectId, userId);
			if (project == null) {
				response.put("code", HttpStatus.BAD_REQUEST.value());
				response.put("message",
						"Requested project with Id: " + projectId + " for User with Id: " + userId + " not found.");

				return ResponseEntity.ok().body(response);
			}

			Optional<EngFlow> engFlowOp = engFlowService.getFlowsForProjectAndId(projectId, engFlowId);
			if (engFlowOp.isPresent()) {
				List<Integer> engFlowIds = engHelper.getEngineeringFlowIds(projectId, engFlowId);
				log.info("\n\n List of engineering flow ids to be processed further is: {}", engFlowIds);

				if (engFlowIds != null && !engFlowIds.isEmpty()) {
					String lineageQuery = engHelper.getDataLineageQuery(project.getDbSchemaName());
					log.info("Data lineage query is: {}", lineageQuery);

					EntityManager entityManager = null;
					try {
						entityManager = sessionManager.getEntityManager();
						entityManager.setFlushMode(FlushModeType.COMMIT);
						entityManager.getTransaction().begin();

						Map<Integer, List<DataLineageInfo>> flowIdValsMap = new LinkedHashMap<>();
						Query lineageQueryObj = entityManager.createNativeQuery(lineageQuery);
						int count = 0;
						for (int fi : engFlowIds) {
							++count;
							List fiValues = lineageQueryObj.setParameter(1, fi).setParameter(2, fi).setParameter(3, fi)
									.setParameter(4, fi).getResultList();
							List<DataLineageInfo> dlis = new ArrayList<>();
							if (fiValues != null && !fiValues.isEmpty()) {
								Iterator iter = fiValues.iterator();
								while (iter.hasNext()) {
									Object[] objs = (Object[]) iter.next();
									DataLineageInfo dli = new DataLineageInfo();
									dli.setFileId((String) objs[1]);

									String src = (String) objs[2];
									if ("agg".equals(src) || src.contains("join")) {
										src = src + "_" + count;
									}

									String trgt = (String) objs[3];
									if ("agg".equals(trgt) || trgt.contains("join")) {
										trgt = trgt + "_" + count;
									}

									dli.setSource(src);
									dli.setTarget(trgt);
									dli.setCategory((String) objs[4]);
									dli.setMetadata((String) objs[5]);
									dli.setFileType((String) objs[6]);
									dli.setRules((String) objs[7]);

									dlis.add(dli);
								}
							}

							flowIdValsMap.put(fi, dlis);
						}

						entityManager.flush();
						entityManager.getTransaction().commit();

						List<DataLineageInfo> finalLineageRes = new ArrayList<>();
						if (flowIdValsMap != null && !flowIdValsMap.isEmpty()) {
							Collection<List<DataLineageInfo>> values = flowIdValsMap.values();
							values.stream().forEach((coll) -> finalLineageRes.addAll(coll));
						}

						response = helper.createSuccessMessage(HttpStatus.OK.value(),
								"Successfully returned Eng Flow data lineage information", finalLineageRes);
					} catch (Exception e) {
						response = helper.failureMessage(500, e.getMessage());
						log.info(
								"Exception occurred while processing the data lineage information for the Engineering flow: {}",
								e.getMessage());

						entityManager.flush();
						entityManager.getTransaction().rollback();
					} finally {
						if (entityManager != null) {
							entityManager.close();
						}
					}
				}
			} else {
				response.put("code", HttpStatus.BAD_REQUEST.value());
				response.put("message", "Engineering flow with Id: " + engFlowId + " not found.");
				response = helper.failureMessage(HttpStatus.BAD_REQUEST.value(),
						"Engineering flow with Id: " + engFlowId + " not found.");
			}

			return ResponseEntity.ok().body(response);
		} catch (final SQLException e) {
			response = helper.failureMessage(422, e.getMessage());
			log.error("Exception Occurred {}", e.getMessage());

			return ResponseEntity.unprocessableEntity().body(response);
		} catch (final Exception e) {
			response = helper.failureMessage(500, e.getMessage());
			log.error("Exception Occurred {}", e.getMessage());

			return ResponseEntity.unprocessableEntity().body(response);
		}
	}

}
