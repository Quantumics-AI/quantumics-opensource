/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsRedashDashboard;
import ai.quantumics.api.req.CreateDashboardRequest;
import ai.quantumics.api.service.ProjectService;
import ai.quantumics.api.service.RedashDashboardService;
import ai.quantumics.api.util.DbSessionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@Api(value = "QuantumSpark Service API ")
public class DashboardController {

    private final DbSessionUtil dbUtil;
    private final RedashDashboardService redashDashboardService;
    private final ProjectService projectService;

    public DashboardController(DbSessionUtil dbUtil, RedashDashboardService redashDashboardService, ProjectService projectService) {
        this.dbUtil = dbUtil;
        this.redashDashboardService = redashDashboardService;
        this.projectService = projectService;
    }


    @ApiOperation(value = "dashboard", response = Json.class, notes = "Create a dashboard by sending request json in body")
    @PostMapping
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Dashboard created successfully"),
            @ApiResponse(code = 400, message = "Error creating the dashboard"),
            @ApiResponse(code = 409, message = "Data May already existed")})
    public ResponseEntity<Object> createDashboard(@RequestBody final CreateDashboardRequest request) {
        ResponseEntity<Object> response = null;
        log.info("Dashboard creation started");
        log.info("Dashboard id : " + request.getDashboardId());
        log.info("Dashboard Name :" + request.getDashboardName());
        log.info("Redash Key :" + request.getRedashKey());
        log.info("Project Id : " + request.getProjectId());
        log.info("UserId : " + request.getUserId());
        try {
            dbUtil.changeSchema("public");
            final Projects projects = projectService.getProject(request.getProjectId());
            dbUtil.changeSchema(projects.getDbSchemaName());

            QsRedashDashboard qsRedashDashboard = new QsRedashDashboard();
            qsRedashDashboard.setDashboardId(request.getDashboardId());
            qsRedashDashboard.setDashboardName(request.getDashboardName());
            qsRedashDashboard.setRdKey(request.getRedashKey());
            qsRedashDashboard.setCreationDate(QsConstants.getCurrentUtcDate());
            qsRedashDashboard.setActive(true);
            redashDashboardService.saveDashboard(qsRedashDashboard);
            log.info("Dashboard saved for projectId :"  + request.getProjectId());
            response = returnResInstance(HttpStatus.OK, "Dashboard created successfully.", null);
        } catch (Exception e) {
            response = returnResInstance(HttpStatus.BAD_REQUEST, "Dashboard created failed.", null);
            log.error("Exception while creating the dashboard {}", e.getMessage());
        }

        return response;
    }

    @ApiOperation(value = "Delete a Dashboard", response = Json.class, notes = "Delete a Dashboard")
    @DeleteMapping("/{dashboardId}/{redashKey}/{projectId}")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Dashboard deleted successfully"),
                    @ApiResponse(code = 404, message = "Dashbboard not found")
            })
    public ResponseEntity<Object> deleteDashboard(
            @PathVariable(value = "dashboardId") final int dashboardId,
            @PathVariable(value = "redashKey") final String redashKey,
            @PathVariable(value = "projectId") final int projectId) {
        ResponseEntity<Object> response = null;
        log.info("Dashboard deletion started");
        log.info("Dashboard Id : " + dashboardId);
        log.info("Redash Key :" + redashKey);
        log.info("Project Id : " + projectId);
        try {
            dbUtil.changeSchema("public");
            final Projects projects = projectService.getProject(projectId);
            dbUtil.changeSchema(projects.getDbSchemaName());

            QsRedashDashboard qsRedashDashboard = redashDashboardService.getDashboardForDashboardIdAndRdKey(dashboardId, redashKey);
            if(qsRedashDashboard != null && qsRedashDashboard.isActive()) {
                qsRedashDashboard.setActive(false);
                redashDashboardService.saveDashboard(qsRedashDashboard);
                log.info("Dashboard deleted for projectId :"  + projectId);
                response = returnResInstance(HttpStatus.OK, "Dashboard deleted successfully.", null);
            } else {
                log.info("No active dashboard found for for projectId :"  + projectId + " and dashboardId : " + dashboardId);
                response = returnResInstance(HttpStatus.BAD_REQUEST, "No active dashboard found for for projectId :"  + projectId + " and dashboardId : " + dashboardId, null);
            }

        } catch (Exception e) {
            response = returnResInstance(HttpStatus.BAD_REQUEST, "Dashboard deletion failed.", null);
            log.error("Exception while creating the dashboard {}", e.getMessage());
        }

        return response;
    }

    private ResponseEntity<Object> returnResInstance(HttpStatus code, String message, Object result) {
        HashMap<String, Object> genericResponse = new HashMap<>();
        genericResponse.put("code", code.value());
        genericResponse.put("message", message);

        if (result != null) {
            genericResponse.put("result", result);
        }
        return ResponseEntity.status(code).body(genericResponse);
    }

}
