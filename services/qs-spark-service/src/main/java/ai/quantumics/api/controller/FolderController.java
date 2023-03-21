/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import java.sql.SQLException;
import java.util.*;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.constants.AuditEventType;
import ai.quantumics.api.constants.AuditEventTypeAction;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.Pipeline;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFiles;
import ai.quantumics.api.model.QsFolders;
import ai.quantumics.api.model.QsNotifications;
import ai.quantumics.api.model.QsPartition;
import ai.quantumics.api.model.QsUserV2;
import ai.quantumics.api.req.UpdateFolderReq;
import ai.quantumics.api.service.FileService;
import ai.quantumics.api.service.FolderService;
import ai.quantumics.api.service.IngestPipelineService;
import ai.quantumics.api.service.NotificationsService;
import ai.quantumics.api.service.PartitionService;
import ai.quantumics.api.service.ProjectService;
import ai.quantumics.api.service.UserServiceV2;
import ai.quantumics.api.util.DbSessionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@RestController
@RequestMapping("/api/v1/folders")
@Api(value = "QuantumSpark Service API ")
public class FolderController {
  private final DbSessionUtil dbUtil;
  private final UserServiceV2 userServiceV2;
  private final FolderService folderService;
  private final FileService fileService;
  private final ProjectService projectService;
  private final NotificationsService notificationsService;
  private final AwsAdapter awsAdapter;
  private final PartitionService partitionService;
  private final ControllerHelper controllerHelper;
  private final IngestPipelineService ingestPipelineService;

  public FolderController(
      final DbSessionUtil dbUtilCi,
      final UserServiceV2 userServiceV2Ci,
      final FolderService folderServiceCi,
      FileService fileServiceCi,
      final ProjectService projectServiceCi,
      final NotificationsService notificationsServiceCi,
      final AwsAdapter awsAdapterCi,
      final PartitionService partitionServiceCi,
      ControllerHelper controllerHelperCi,
      final IngestPipelineService ingestPipelineServiceCi) {
    dbUtil = dbUtilCi;
    userServiceV2 = userServiceV2Ci;
    folderService = folderServiceCi;
    fileService = fileServiceCi;
    projectService = projectServiceCi;
    notificationsService = notificationsServiceCi;
    awsAdapter = awsAdapterCi;
    partitionService = partitionServiceCi;
    controllerHelper = controllerHelperCi;
    ingestPipelineService = ingestPipelineServiceCi;
  }

  @ApiOperation(value = "folders", response = Json.class)
  @PostMapping
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Folder Created Successfully..!"),
        @ApiResponse(code = 400, message = "Bad request!")
      })
  public ResponseEntity<Object> createFolder(@RequestBody final QsFolders folder) {
    final Map<String, Object> response = new HashMap<>();
    try {
      dbUtil.changeSchema("public");
      final Projects projects = projectService.getProject(folder.getProjectId());
      dbUtil.changeSchema(projects.getDbSchemaName());
      
      folder.setCreatedDate(QsConstants.getCurrentUtcDate());
      folder.setCreatedBy(Integer.toString(folder.getUserId()));
      folder.setActive(true);
      
      if(folderService.isFolderNameAvailable(folder.getFolderName())) {
        folder.setFolderDisplayName(folder.getFolderName());
        final QsFolders saveFolders = folderService.saveFolders(folder);
        
        String auditMessage = controllerHelper.getAuditMessage(QsConstants.AUDIT_FOLDER_MSG,
            AuditEventTypeAction.CREATE.getAction().toLowerCase());
        controllerHelper.recordAuditEvent(AuditEventType.INGEST, AuditEventTypeAction.CREATE,
            String.format(auditMessage, saveFolders.getFolderDisplayName()), null,
            saveFolders.getUserId(), projects.getProjectId());

        response.put("code", HttpStatus.SC_OK); // TODO 201
        response.put("result", saveFolders);
        response.put("message", "Folder created successfully..!");
      } else {
        response.put("code", HttpStatus.SC_CONFLICT); 
        response.put("result", "Folder with name '"+folder.getFolderName()+"' already exists.");
        response.put("message", "Folder with name '"+folder.getFolderName()+"' already exists.");
        return ResponseEntity.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).body(response);
      }
      
      
      return ResponseEntity.ok().body(response);

    } catch (final SQLException ex) {
      response.put("code", HttpStatus.SC_BAD_REQUEST);
      response.put("message", "Data insert Failed!");
      log.error("Exception while creating the folder {}", ex.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * @param userId
   * @param userId
   * @param message
   * @throws SQLException
   */
  @SuppressWarnings("unused")
  private void createNotification(int projectId, int userId, String message)
      throws SQLException {
    QsNotifications notification = new QsNotifications();
    notification.setNotificationMsg(message);
    notification.setAdminMsg(true);
    notification.setCreationDate(DateTime.now().toDate());
    notification.setNotificationRead(false);
    dbUtil.changeSchema("public");
    
    notification.setProjectId(projectId);
    notification.setUserId(userId);
    notificationsService.save(notification);
  }

  @ApiOperation(value = "Delete a Folder", response = Json.class, notes = "Delete a folder")
  @DeleteMapping("/{projectId}/{userId}/{folderId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Folder Deleted successfully"),
        @ApiResponse(code = 404, message = "Folder Not Found")
      })
  public ResponseEntity<Object> deleteFolder(
      @PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "userId") final int userId,
      @PathVariable(value = "folderId") final int folderId) {
    
    final Map<String, Object> deleteResponse = new HashMap<>();
    try {
      
      dbUtil.changeSchema("public");
      Projects project = projectService.getProject(projectId);
      QsUserV2 user = userServiceV2.getActiveUserById(userId);
      String userName = user.getQsUserProfile().getUserFirstName()+" "+user.getQsUserProfile().getUserLastName();
      dbUtil.changeSchema(project.getDbSchemaName());
      DateTime now = DateTime.now();
      QsFolders folder = folderService.getFolder(folderId);
      folder.setActive(false);
      folder.setModifiedBy(userName);
      folder.setModifiedDate(now.toDate());
      
      folderService.saveFolders(folder);

      // Now delete the Folder in S3 and also Athena table.
      List<QsFiles> files = fileService.getFiles(projectId, folderId);
      
      if(files != null && !files.isEmpty()) {
        files.stream().forEach((file) -> {
          try {
            Optional<QsPartition> partitionOp = partitionService.getPartitionByFileId(folderId, file.getFileId());
            if(partitionOp.isPresent()) {
              QsPartition partition = partitionOp.get();
              String bucketName = project.getBucketName()+"-"+QsConstants.RAW;
              String objectKey = String.format("%s/%s/%s", folder.getFolderName(), partition.getPartitionName(), file.getFileName());
              awsAdapter.deleteObject(bucketName, objectKey);
            }
            
            file.setActive(false);
            fileService.saveFilesCollection(files);
          }catch(SQLException se) {
            
          }
        });
      }
      
      awsAdapter.athenaDropTableQuery(project.getRawDb(), folder.getFolderName().toLowerCase());
      
      // Insert a record into the Notifications table..
      String auditMessage = controllerHelper.getAuditMessage(QsConstants.AUDIT_FOLDER_MSG, AuditEventTypeAction.DELETE.getAction().toLowerCase());
      String notification = controllerHelper.getNotificationMessage(QsConstants.AUDIT_FOLDER_MSG, AuditEventTypeAction.DELETE.getAction().toLowerCase());
      controllerHelper.recordAuditEvent(AuditEventType.INGEST, AuditEventTypeAction.DELETE, 
          String.format(auditMessage, folder.getFolderDisplayName()), 
          String.format(notification, folder.getFolderDisplayName(),
              userName), user.getUserId(), project.getProjectId());

      deleteResponse.put("code", HttpStatus.SC_OK);
      deleteResponse.put(
          "message", String.format("Folder %s and its files were deleted", folder.getFolderName()));

    } catch (SQLException|InterruptedException exception) {
      deleteResponse.put("code", HttpStatus.SC_INTERNAL_SERVER_ERROR);
      deleteResponse.put("message", String.format("Error while deleting from folder %s", exception.getMessage()));
    }
    return ResponseEntity.status(HttpStatus.SC_OK).body(deleteResponse);
  }

  @ApiOperation(value = "folders", response = Json.class)
  @GetMapping("/{projectId}/{userId}")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "List All QsFolders for Project ID")})
  public ResponseEntity<Object> getFolders(
          @PathVariable(value = "projectId") final int projectId,
          @PathVariable(value = "userId") final int userId,
          @RequestParam(value = "pipeline", required = false) Boolean pipeline) {
    dbUtil.changeSchema("public");
    final Projects project = projectService.getProject(projectId);
    final Map<String, Object> response = new HashMap<>();
    dbUtil.changeSchema(project.getDbSchemaName());
    try {
      List<QsFolders> qsFolders = new ArrayList<>();
      if(ObjectUtils.isNotEmpty(pipeline)) {
        qsFolders = folderService.allFolders(userId, projectId, pipeline.booleanValue());
      } else {
        qsFolders = folderService.allFolders(userId, projectId);
      }
      qsFolders.stream().forEach((folder) -> {
        if (folder.getFolderDisplayName() != null && !folder.getFolderDisplayName().isEmpty()) {
          folder.setFolderName(folder.getFolderDisplayName());
        }
        folder.setFilesCount(fileService.getFileCount(folder.getFolderId()));
      });
      response.put("code", HttpStatus.SC_OK);
      response.put("message", "Folders Listed Successfully");
      response.put("projectName", project.getProjectDisplayName());
      response.put("result", qsFolders);
    } catch (SQLException exception) {
      response.put("code", HttpStatus.SC_INTERNAL_SERVER_ERROR);
      response.put("message", "Error -" + exception.getMessage());
    }
    return ResponseEntity.ok().body(response);
  }

  @ApiOperation(
      value = "Update a Folder",
      response = Json.class,
      notes = "Update a folder details but not name")
  @PutMapping("/{projectId}/{userId}/{folderId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Folder updated successfully"),
        @ApiResponse(code = 404, message = "Folder Not Found")
      })
  public ResponseEntity<Object> updateFolder(
      @RequestBody final UpdateFolderReq updateFolder,
      @PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "userId") final int userId,
      @PathVariable(value = "folderId") final int folderId) {
    final Map<String, Object> updateResponse = new HashMap<>();
    try {
      dbUtil.changeSchema("public");
      final Projects project = projectService.getProject(projectId);
      QsUserV2 qsUser = userServiceV2.getUserById(userId);
      dbUtil.changeSchema(project.getDbSchemaName());
      final QsFolders folder = folderService.getFolder(folderId);
      folder.setFolderDesc(updateFolder.getFolderDesc());
      folder.setModifiedDate(QsConstants.getCurrentUtcDate());
      folder.setModifiedBy(Integer.toString(userId));

      if(updateFolder.getFolderDisplayName() !=null && 
          !updateFolder.getFolderDisplayName().equalsIgnoreCase(folder.getFolderDisplayName())) {
        if(folderService.isFolderNameAvailable(updateFolder.getFolderDisplayName())) {
          folder.setFolderDisplayName(updateFolder.getFolderDisplayName());
        } else {
          updateResponse.put("code", HttpStatus.SC_CONFLICT); 
          updateResponse.put("result", "Folder with name '"+folder.getFolderDisplayName()+"' already exists.");
          updateResponse.put("message", "Folder with name '"+folder.getFolderDisplayName()+"' already exists.");
          return ResponseEntity.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).body(updateResponse);
        } 
      } 
      folderService.saveFolders(folder);
      String auditMessage = controllerHelper.getAuditMessage(QsConstants.AUDIT_FOLDER_MSG, AuditEventTypeAction.UPDATE.getAction().toLowerCase());
      String notification = null;
      controllerHelper.recordAuditEvent(AuditEventType.INGEST, AuditEventTypeAction.UPDATE, 
          String.format(auditMessage, folder.getFolderDisplayName()), 
          notification, 
          qsUser.getUserId(), project.getProjectId());
      
      updateResponse.put("code", HttpStatus.SC_OK);
      updateResponse.put("message", "Folder Details Updated");
      updateResponse.put("result", folder);
      return ResponseEntity.ok().body(updateResponse);
    } catch (final SQLException ex) {
      updateResponse.put("code", HttpStatus.SC_UNPROCESSABLE_ENTITY);
      updateResponse.put("message", ex.getMessage());
      return ResponseEntity.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).body(updateResponse);
    }
  }
}
