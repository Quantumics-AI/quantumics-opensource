/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.EngFlowEvent;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.service.EngFlowEventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@RestController
@RequestMapping("/api/v1/preview")
@Api(value = "QuantumSpark Service API ")
public class PreviewController {

  private final EngFlowEventService flowEventService;
  private final ControllerHelper helper;

  public PreviewController(
      EngFlowEventService engFlowEventService, ControllerHelper controllerHelper) {
    flowEventService = engFlowEventService;
    helper = controllerHelper;
  }

  @ApiOperation(value = "Get data for event in preview mode", response = Json.class)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Data rendered"),
        @ApiResponse(code = 500, message = "Data Not found!")
      })
  @GetMapping(value = "/{projectId}/{engFlowId}/{autoConfigEventId}")
  public ResponseEntity<Object> getEventData(
      @PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "engFlowId") final int engFlowId,
      @PathVariable(value = "autoConfigEventId") final int autoConfigEventId) {
    Map<String, Object> response;
    try {
      helper.getProjectAndChangeSchema(projectId);
      Optional<EngFlowEvent> flowEvent =
          flowEventService.getFlowEventForEngFlowAndConfigId(engFlowId, autoConfigEventId);
      response =
          flowEvent
              .map(
                  event ->
                      helper.createSuccessMessage(
                          HttpStatus.OK.value(), "Data rendered successfully", event))
              .orElseGet(
                  () ->
                      helper.createSuccessMessage(
                          HttpStatus.OK.value(), "Empty - Zero records found!", "{}"));
    } catch (final SQLException e) {
      response = helper.failureMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      log.error("Error {} and {}", e.getErrorCode(), e.getMessage());
    }
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @ApiOperation(value = "Get data for event in preview mode", response = Json.class)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Data rendered"),
        @ApiResponse(code = 500, message = "Data Not found!")
      })
  @GetMapping(value = "/all/{projectId}/{userId}/{engFlowId}")
  public ResponseEntity<Object> getAllEventsData(
      @PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "userId") final int userId,
      @PathVariable(value = "engFlowId") final int engFlowId) {
    Map<String, Object> response = new HashMap<>();
    try {
      Projects project = helper.getProjects(projectId, userId);
      if(project == null) {
        response.put("code", HttpStatus.BAD_REQUEST);
        response.put("message", "Requested project with Id: "+ projectId +" for User with Id: " + userId +" not found.");
        
        return ResponseEntity.ok().body(response);
      }
      
      List<EngFlowEvent> allEventsData = flowEventService.getAllEventsData(engFlowId);
      boolean flag = false;
      if(allEventsData != null && !allEventsData.isEmpty()) {
        for(EngFlowEvent event : allEventsData) {
          if(event.getEngFlowEventData() == null || event.getEngFlowEventData().isEmpty()) {
            // Set the Trigger flag to True, so that Event is sent again by the UI.
            flag = true;
            break;
          }
        }
      }
      
      response.put("code", HttpStatus.OK.value());
      response.put("result", allEventsData);
      response.put("message", "Data rendered successfully.");
      response.put("trigger_next", flag);
      
    } catch (final SQLException e) {
      response = helper.failureMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      log.error("Error {} and {}", e.getErrorCode(), e.getMessage());
    }
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
