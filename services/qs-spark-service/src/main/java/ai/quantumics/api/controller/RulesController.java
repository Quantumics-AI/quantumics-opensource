/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ai.quantumics.api.constants.AuditEventType;
import ai.quantumics.api.constants.AuditEventTypeAction;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.CleansingParam;
import ai.quantumics.api.model.CleansingRule;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFiles;
import ai.quantumics.api.req.CleansingParamRequest;
import ai.quantumics.api.req.RuleSequenceUpdateRequest;
import ai.quantumics.api.service.CleansingRuleParamService;
import ai.quantumics.api.service.CleansingRuleService;
import ai.quantumics.api.service.FileService;
import ai.quantumics.api.service.ProjectService;
import ai.quantumics.api.util.DbSessionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@RestController
@RequestMapping("/api/v1/rules")
@Api(value = "QuantumSpark Service API ")
public class RulesController {

  private final DbSessionUtil dbUtil;
  private final ProjectService projectService;
  private final CleansingRuleService cleanseRuleService;
  private final CleansingRuleParamService cleanseRuleParamService;
  private final ControllerHelper controllHelper;
  private final FileService fileService;

  public RulesController(
      final DbSessionUtil dbUtilCi,
      final ProjectService projectServiceCi,
      final CleansingRuleService cleanseRuleServiceCi,
      final CleansingRuleParamService cleanseRuleParamServiceCi,
      final ControllerHelper controllHelperCi,
      final FileService fileServiceCi) {
    dbUtil = dbUtilCi;
    projectService = projectServiceCi;
    cleanseRuleService = cleanseRuleServiceCi;
    cleanseRuleParamService = cleanseRuleParamServiceCi;
    controllHelper = controllHelperCi;
    fileService = fileServiceCi;
  }

  @ApiOperation(value = "Delete Rules", response = Json.class)
  @PutMapping("/param/{projectId}/{userId}/{ruleId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Rule deleted successfully !"),
        @ApiResponse(code = 404, message = "Specific Rule not found")
      })
  public ResponseEntity<Object> deleteRulesParam(
      @PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "userId") final int userId,
      @PathVariable(value = "ruleId") final int ruleId) {
    final Map<String, Object> response = new HashMap<>();
    CleansingParam cleansingParam;
    try {
      dbUtil.changeSchema("public");
      final Projects project = projectService.getProject(projectId, userId);
      if(project == null) {
        response.put("code", HttpStatus.SC_BAD_REQUEST);
        response.put("message", "Requested project with Id: "+ projectId +" for User with Id: " + userId +" not found.");
        
        return ResponseEntity.ok().body(response);
      }
      
      dbUtil.changeSchema(project.getDbSchemaName());
      Optional<CleansingParam> ruleById = cleanseRuleParamService.getRuleById(ruleId);
      if (ruleById.isPresent()) {
        cleansingParam = ruleById.get();
        
        // Check whether user can delete this rule or not before deleting...
        // Below are the points to check:
        // 1. Check whether there is any Cleanse Job executed on this file.
        // 2. If so, then, check whether the rule to be deleted falls in the category of:
        //      countMatch, split, mergeRule, manageColumns, extractColumnValues
        // 3. If so, don't delete the rule and throw the validation error.
        /*int folderId = cleansingParam.getFolderId();
        int jobCount = runJobService.getSucceededJobsOnlyInFolder(userId, projectId, folderId);
        if(jobCount > 0) {
          // Cleanse Job is executed on a file present in this folder and it is successful.
          
          String ruleName = cleansingParam.getRuleInputLogic();
          List<String> ruleNames = Arrays.asList(QsConstants.NEW_COL_GENERATING_RULES);
          if(ruleNames.contains(ruleName)) {
            response.put("code", 400);
            response.put("message", "Cleansing Rule: "+ cleansingParam.getRuleInputLogic() + " cannot be deleted, as there are cleanse jobs associated with this rule for the Folder Id: "+ cleansingParam.getFolderId());
            
            return ResponseEntity.status(HttpStatus.SC_OK).body(response);
          }
        }*/
        
        final int ruleSequence = cleansingParam.getRuleSequence();
        cleansingParam.setActive(false);
        cleansingParam.setRuleSequence(0);        
        cleansingParam = cleanseRuleParamService.saveRuleWithParam(cleansingParam);
        final int fileId = cleansingParam.getFileId();
        sortSequence(cleanseRuleParamService.getSeqGreaterThan(fileId, ruleSequence));
        
        Optional<QsFiles> fileOp = fileService.getFileById(fileId);
        if(fileOp.isPresent()) {
          String auditMessage = controllHelper.getAuditMessage(AuditEventType.CLEANSE, AuditEventTypeAction.DELETE);
          controllHelper.recordAuditEvent(AuditEventType.CLEANSE, AuditEventTypeAction.DELETE, 
              String.format(auditMessage, fileOp.get().getFileName()), 
              null, userId, project.getProjectId());
        }
        
        response.put("code", 200);
        response.put("message", "Rule Deleted Successfully");
      } else {
        response.put("code", 404);
        response.put("message", "Zero Rules Found!");
      }
    } catch (final Exception e) {
      response.put("code", 404);
      response.put("message", "Specific Rule not found" + e.getMessage());
    }
    return ResponseEntity.status(HttpStatus.SC_OK).body(response);
  }

  @ApiOperation(value = "rules", response = Json.class)
  @GetMapping("/{projectId}/{folderId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Rule(s) Retrieved Successfully !"),
        @ApiResponse(code = 404, message = "Specific Rule not found")
      })
  public ResponseEntity<Object> getRules(
      @PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "folderId") final int folderId) {
    dbUtil.changeSchema("public");
    final Projects project = projectService.getProject(projectId);
    dbUtil.changeSchema(project.getDbSchemaName());
    final Map<String, Object> response = new HashMap<>();
    final List<CleansingRule> rulesForFolder = cleanseRuleService.getRulesForFolder(folderId);
    if (rulesForFolder.isEmpty()) {
      response.put("code", 404);
      response.put("message", "Specific Rule not found");
    } else {
      response.put("code", 200);
      response.put("result", rulesForFolder);
      response.put("message", "Rule(s) Retrieved Successfully");
    }
    return ResponseEntity.ok().body(response);
  }

  @ApiOperation(value = "rules catalogue", response = Json.class)
  @GetMapping("/catalogue/{projectId}/{userId}/{folderId}/{fileId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Rule(s) Retrieved Successfully !"),
        @ApiResponse(code = 404, message = "Specific Rule not found")
      })
  public ResponseEntity<Object> getRulesCatalogue(
      @PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "userId") final int userId,
      @PathVariable(value = "folderId") final int folderId,
      @PathVariable(value = "fileId") final int fileId) {
    
    final Map<String, Object> response = new HashMap<>();
    
    dbUtil.changeSchema("public");
    final Projects project = projectService.getProject(projectId, userId);
    if(project == null) {
      response.put("code", HttpStatus.SC_BAD_REQUEST);
      response.put("message", "Requested project with Id: "+ projectId +" for User with Id: " + userId +" not found.");
      
      return ResponseEntity.ok().body(response);
    }
    
    dbUtil.changeSchema(project.getDbSchemaName());
    final List<CleansingParam> rulesCatalogue =
        cleanseRuleParamService.getRulesOrderByRuleSequence(folderId, fileId);
    if (!rulesCatalogue.isEmpty()) {
      response.put("code", 200);
      response.put("result", rulesCatalogue);
      response.put("message", "Rule(s) Retrieved Successfully");
    } else {
      response.put("code", 404);
      response.put("message", "Specific Rule not found");
    }
    return ResponseEntity.ok().body(response);
  }

  private void impactedColumns(
      final CleansingParamRequest cleanseRuleParamReq,
      final CleansingParam cleanseRuleParam,
      final int ruleSequence, final boolean createFlag)
      throws Exception {
    cleanseRuleParam.setRuleImpactedCols(
        joinListWithComma(cleanseRuleParamReq.getRuleImpactedCols()));
    cleanseRuleParam.setRuleInputValues(cleanseRuleParamReq.getRuleInputValues());
    cleanseRuleParam.setRuleInputLogic(cleanseRuleParamReq.getRuleInputLogic());
    cleanseRuleParam.setRuleOutputValues(cleanseRuleParamReq.getRuleOutputValues());
    cleanseRuleParam.setRuleDelimiter(cleanseRuleParamReq.getRuleDelimiter());
    cleanseRuleParam.setRuleSequence(ruleSequence);
    cleanseRuleParam.setFolderId(cleanseRuleParamReq.getFolderId());
    
    if(cleanseRuleParamReq.getRuleInputLogic1()!= null) {
      cleanseRuleParam.setRuleInputLogic1(cleanseRuleParamReq.getRuleInputLogic1());
    }
    
    if(cleanseRuleParamReq.getRuleInputLogic2()!= null) {
      cleanseRuleParam.setRuleInputLogic2(cleanseRuleParamReq.getRuleInputLogic2());
    }
    
    if(cleanseRuleParamReq.getRuleInputLogic3()!= null) {
      cleanseRuleParam.setRuleInputLogic3(cleanseRuleParamReq.getRuleInputLogic3());
    }
    
    if(cleanseRuleParamReq.getRuleInputLogic4()!= null) {
      cleanseRuleParam.setRuleInputLogic4(cleanseRuleParamReq.getRuleInputLogic4());
    }
    
    if(cleanseRuleParamReq.getRuleInputLogic5()!= null) {
      cleanseRuleParam.setRuleInputLogic5(cleanseRuleParamReq.getRuleInputLogic5());
    }
    
    if (cleanseRuleParamReq.getRuleInputValues1() != null) {
      cleanseRuleParam.setRuleInputValues1(cleanseRuleParamReq.getRuleInputValues1());
    }
    
    if (cleanseRuleParamReq.getRuleInputValues2() != null) {
      cleanseRuleParam.setRuleInputValues2(cleanseRuleParamReq.getRuleInputValues2());
    }
    
    if (cleanseRuleParamReq.getRuleInputValues3() != null) {
      cleanseRuleParam.setRuleInputValues3(cleanseRuleParamReq.getRuleInputValues3());
    }
    
    if (cleanseRuleParamReq.getRuleInputValues4() != null) {
      cleanseRuleParam.setRuleInputValues4(cleanseRuleParamReq.getRuleInputValues4());
    }
    
    if (cleanseRuleParamReq.getRuleInputValues5() != null) {
      cleanseRuleParam.setRuleInputValues5(cleanseRuleParamReq.getRuleInputValues5());
    }
    
    cleanseRuleParam.setParentRuleIds(cleanseRuleParamReq.getParentRuleIds());
    
    if(createFlag) {
      cleanseRuleParam.setCreationDate(QsConstants.getCurrentUtcDate());
    } 
    else {
      cleanseRuleParam.setModifiedDate(QsConstants.getCurrentUtcDate());
    }
    
    if (cleanseRuleParamReq.getRuleInputNewcolumns() != null) {
      cleanseRuleParam.setRuleInputNewcolumns(cleanseRuleParamReq.getRuleInputNewcolumns());
    }
    
    cleanseRuleParam.setActive(true);
  }

  private String joinListWithComma(final ArrayList<String> tempData) {
    return String.join(",", tempData);
  }

  @ApiOperation(value = "rules", response = Json.class)
  @PostMapping("/{projectId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Rule Saved Successfully !"),
        @ApiResponse(code = 409, message = "Rule Already exists !")
      })
  public ResponseEntity<Object> saveRules(
      @RequestBody final CleansingRule cleanseRule,
      @PathVariable(value = "projectId") final int projectId) {
    log.info("Invoking save Rule API {}", cleanseRule.toString());
    dbUtil.changeSchema("public");
    final Projects project = projectService.getProject(projectId);
    dbUtil.changeSchema(project.getDbSchemaName());
    final Map<String, Object> response = new HashMap<>();
    final CleansingRule saveRule =
        cleanseRuleService.saveRule(cleanseRule); // TODO Check with existing rule
    if (saveRule != null) {
      response.put("code", 200);
      response.put("result", saveRule);
      response.put("message", "Saved successfully !");
    } else {
      response.put("code", 409);
      response.put("message", "Failed to save the rule");
    }
    return ResponseEntity.ok().body(response);
  }

  @ApiOperation(value = "rules", response = Json.class)
  @PostMapping("/param/{projectId}/{userId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "OKAY"),
        @ApiResponse(code = 409, message = "Failure message")
      })
  public ResponseEntity<Object> saveRulesParam(
      @RequestBody final CleansingParamRequest cleanseRuleParamReq,
      @PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "userId") final int userId)
      throws Exception {
    final Map<String, Object> response = new HashMap<>();
    log.info("Invoking cleanseRuleWithParam  API {}", cleanseRuleParamReq.toString());
    try {
      dbUtil.changeSchema("public");
      final Projects project = projectService.getProject(projectId, userId);
      if(project == null) {
        response.put("code", HttpStatus.SC_BAD_REQUEST);
        response.put("message", "Requested project with Id: "+ projectId +" for User with Id: " + userId +" not found.");
        
        return ResponseEntity.ok().body(response);
      }
      
      dbUtil.changeSchema(project.getDbSchemaName());
      final CleansingParam cleanseRuleParam = new CleansingParam();
      final int fileId = cleanseRuleParamReq.getFileId();
      final int folderId = cleanseRuleParamReq.getFolderId();
      final int sequenceLength = cleanseRuleParamService.getRulesInfo(folderId, fileId).size();
      final int ruleSequence = sequenceLength + 1;
      log.info("Adding a new sequence - to the rule {}", ruleSequence);
      impactedColumns(cleanseRuleParamReq, cleanseRuleParam, ruleSequence, true);
      cleanseRuleParam.setFileId(fileId);
      cleanseRuleParamService.saveRuleWithParam(cleanseRuleParam);
      
      String auditMessage = controllHelper.getAuditMessage(AuditEventType.CLEANSE, AuditEventTypeAction.CREATE);
      Optional<QsFiles> fileOp = fileService.getFileById(fileId);
      controllHelper.recordAuditEvent(AuditEventType.CLEANSE, AuditEventTypeAction.CREATE, 
          String.format(auditMessage, fileOp.get().getFileName()), 
          null, userId, project.getProjectId());
      response.put("code", HttpStatus.SC_OK);
      response.put("message", "Data saved successfully");
    } catch (final Exception ex) {
      response.put("code", HttpStatus.SC_CONFLICT);
      response.put("message", "Conflict with existing details or " + ex.getMessage());
      // return ResponseEntity.status(HttpStatus.SC_CONFLICT).body(response);
    }
    return ResponseEntity.ok().body(response);
  }

  @Async("qsThreadPool")
  void sortSequence(final List<CleansingParam> list) {
    int tempId = 0;
    log.info("Sorting rule sequence of size {}", list.size());
    try {
      for (final CleansingParam cleansingParam : list) {
        tempId = cleansingParam.getCleansingParamId();
        Optional<CleansingParam> ruleById = cleanseRuleParamService.getRuleById(tempId);
        if (ruleById.isPresent()) {
          final CleansingParam tempObj = ruleById.get();
          tempObj.setRuleSequence(tempObj.getRuleSequence() - 1);
          cleanseRuleParamService.saveRuleWithParam(tempObj);
        }
      }
    } catch (final Exception e) {
      log.error("Exception sorting rule sequence at " + tempId + " - " + e.getMessage());
    }
  }

  @ApiOperation(value = "rules", response = Json.class)
  @PutMapping("/param/{projectId}/{userId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Rule updated successfully !"),
        @ApiResponse(code = 400, message = "Sequence Id can not to be empty or zero !"),
        @ApiResponse(code = 422, message = "Unprocessable Entity")
      })
  public ResponseEntity<Object> updateRulesParam(
      @RequestBody final CleansingParamRequest cleanseRuleParamReq,
      @PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "userId") final int userId) {
    log.info("Invoking cleanseRuleWithParam  API {}", cleanseRuleParamReq.toString());
    final int ruleSequence = cleanseRuleParamReq.getRuleSequence();
    final Map<String, Object> response = new HashMap<>();
    if (ruleSequence == 0) {
      response.put("code", 400);
      response.put("message", "Sequence Id can not be empty or zero !");
      return ResponseEntity.status(400).body(response);
    }
    dbUtil.changeSchema("public");
    final Projects project = projectService.getProject(projectId, userId);
    
    if(project == null) {
      response.put("code", HttpStatus.SC_BAD_REQUEST);
      response.put("message", "Requested project with Id: "+ projectId +" for User with Id: " + userId +" not found.");
      
      return ResponseEntity.ok().body(response);
    }
    
    dbUtil.changeSchema(project.getDbSchemaName());
    try {
      Optional<CleansingParam> ruleById =
          cleanseRuleParamService.getRuleById(cleanseRuleParamReq.getCleansingParamId());
      if (ruleById.isPresent()) {
        final CleansingParam cleanseRuleParam = ruleById.get();
        cleanseRuleParam.setCleansingRuleId(cleanseRuleParamReq.getCleansingRuleId());
        impactedColumns(cleanseRuleParamReq, cleanseRuleParam, ruleSequence, false);
        cleanseRuleParam.setFileId(cleanseRuleParamReq.getFileId());
        cleanseRuleParamService.saveRuleWithParam(cleanseRuleParam);
        String auditMessage = controllHelper.getAuditMessage(AuditEventType.CLEANSE, AuditEventTypeAction.UPDATE);
        Optional<QsFiles> fileOp = fileService.getFileById(cleanseRuleParamReq.getFileId());
        controllHelper.recordAuditEvent(AuditEventType.CLEANSE, AuditEventTypeAction.UPDATE, 
            String.format(auditMessage, fileOp.get().getFileName()), 
            null, userId, project.getProjectId());
        response.put("code", HttpStatus.SC_OK);
        response.put("message", "Rule updated successfully !");
        // return ResponseEntity.ok().body(response);
      }
    } catch (final Exception e) {
      response.put("code", 422);
      response.put("message", "Exception occurred while processing data !" + e.getMessage());
      log.error("Error - {}", e.getMessage());
    }
    return ResponseEntity.status(HttpStatus.SC_OK).body(response);
  }

  @ApiOperation(value = "rules", response = Json.class)
  @PutMapping("/{projectId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Rule sequence updated successfully !"),
        @ApiResponse(code = 404, message = "Failed to update rule sequence ! ")
      })
  public ResponseEntity<Object> updateSequenceInRules(
      @RequestBody @Valid final List<RuleSequenceUpdateRequest> updateReq,
      @PathVariable(value = "projectId") final int projectId) {
    final Map<String, Object> response = new HashMap<>();
    dbUtil.changeSchema("public");
    final Projects project = projectService.getProject(projectId);
    dbUtil.changeSchema(project.getDbSchemaName());
    final List<CleansingParam> listRuleWithParam = new ArrayList<>();
    try {
      for (final RuleSequenceUpdateRequest ruleSequenceUpdate : updateReq) {
        Optional<CleansingParam> ruleById =
            cleanseRuleParamService.getRuleById(ruleSequenceUpdate.getCleansingParamId());
        if (ruleById.isPresent()) {
          final CleansingParam cleansingParam = ruleById.get();
          cleansingParam.setRuleSequence(ruleSequenceUpdate.getRuleSequence());
          log.info(" save {}", cleansingParam.toString());
          listRuleWithParam.add(cleansingParam);
          response.put("code", 200);
          response.put("result", listRuleWithParam);
          response.put("message", "Rule sequence updated successfully ! ");
        } else {
          response.put("code", 200);
          response.put("message", "No Records Founds! ");
        }
      }
      cleanseRuleParamService.saveAll(listRuleWithParam);
    } catch (final Exception e) {
      response.put("code", 409);
      response.put("message", "Failed to update rule sequence ! " + e.getMessage());
      // return ResponseEntity.status(409).body(response);
    }

    return ResponseEntity.ok().body(response);
  }
}
