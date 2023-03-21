package ai.quantumics.api.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import ai.quantumics.api.constants.NotificationStatus;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.model.QsNotifications;
import ai.quantumics.api.model.QsUserV2;
import ai.quantumics.api.service.AuditEventsService;
import ai.quantumics.api.service.NotificationsService;
import ai.quantumics.api.service.UserServiceV2;
import ai.quantumics.api.util.DbSessionUtil;
import ai.quantumics.api.vo.Notification;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@RestController
@Api(value = "QuantumSpark Service API ")
public class NotificationsController {
  private NotificationsService notificationsService;
  private UserServiceV2 userServiceV2;
  private final DbSessionUtil dbUtil;
  private AuditEventsService auditEventsService;
  
  public NotificationsController(NotificationsService notificationsService, UserServiceV2 userServiceV2, DbSessionUtil dbUtil, AuditEventsService auditEventsService) {
    this.notificationsService = notificationsService;
    this.userServiceV2 = userServiceV2;
    this.dbUtil = dbUtil;
    this.auditEventsService = auditEventsService;
  }
  
  @ApiOperation(value = "Notifications", response = Json.class)
  @GetMapping("/api/v1/notifications/{userId}/{type}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Return the list of all the available notifications"),
        @ApiResponse(code = 400, message = "Failed to return the notifications")
      })
  public ResponseEntity<Object> getAllNotifications(@PathVariable(value = "userId") final int userId,
                                          @PathVariable(value = "type") final String type) {
    Map<String, Object> response;
    try {
      log.info("List notifications request received...");
      dbUtil.changeSchema("public");
      
      QsUserV2 user = userServiceV2.getActiveUserById(userId);
      if(user != null) {
        List<Integer> userIds = new ArrayList<>();
        userIds.add(user.getUserId());
        
        if(QsConstants.ADMIN.equals(user.getUserRole())) {
          List<QsUserV2> childUsers = userServiceV2.getSubUsersOfParent(userId);
          if(childUsers != null && !childUsers.isEmpty()) {
            childUsers.stream().forEach((childUser) -> {
              userIds.add(childUser.getUserId());
            });
          }
        }
        
        final List<QsNotifications> notifications = notificationsService.getAllNotifications(userIds, type);
        if (notifications != null && !notifications.isEmpty()) {
          // If notification type is not "All" meaning "Unread" notifications, then, update the 
          // Notification Read flag to boolean true.
          if(!QsConstants.ALL_NOTIFS.equals(type)) {
            notifications.stream().forEach((notif) -> {
              notif.setNotificationRead(true);
              try {
                
                // TODO: We have to think of bulk update here...
                notificationsService.save(notif);
              }catch(SQLException e) {
                // do nothing
              }
            });
          }

          response = createSuccessMessage(HttpStatus.SC_OK, "List of all the available notifications", notifications);
          log.info("Returned {} notifications back to UI layer.", notifications.size());
        }
        else {
          response = createSuccessMessage(HttpStatus.SC_OK, "No notifications found", Collections.emptyList());
        }
      }
      else {
        response = createSuccessMessage(HttpStatus.SC_BAD_REQUEST, "User not found", Collections.emptyList());
      }
    } catch (final Exception ex) {
      response = failureMessage(HttpStatus.SC_BAD_REQUEST, ex.getMessage());
    }
    return ResponseEntity.ok().body(response);
  }
  
  @ApiOperation(value = "Notifications count for a user.", response = Json.class)
  @GetMapping("/api/v1/notifications/{userId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Return the list of all the available notifications"),
        @ApiResponse(code = 400, message = "Failed to return the notifications")
      })
  public ResponseEntity<Object> getNotificationsCountForUser(@PathVariable(value = "userId") final int userId) {
    Map<String, Object> response;
    try {
      dbUtil.changeSchema("public");
      
      QsUserV2 user = userServiceV2.getActiveUserById(userId);
      if(user != null) {
        List<Integer> userIds = new ArrayList<>();
        userIds.add(user.getUserId());
        
        if(QsConstants.ADMIN.equals(user.getUserRole())) {
          List<QsUserV2> childUsers = userServiceV2.getSubUsersOfParent(userId);
          if(childUsers != null && !childUsers.isEmpty()) {
            childUsers.stream().forEach((childUser) -> {
              userIds.add(childUser.getUserId());
            });
          }
        }
        
        final int notifsCount = notificationsService.getNotificationsCountForUsers(userIds);
        response = createSuccessMessage(HttpStatus.SC_OK, "Successful", notifsCount);
      } else {
        response = failureMessage(HttpStatus.SC_BAD_REQUEST, "User not found.");
      }
    } catch (final Exception ex) {
      response = failureMessage(HttpStatus.SC_BAD_REQUEST, ex.getMessage());
    }
    return ResponseEntity.ok().body(response);
  }
  
  
  @ApiOperation(value = "Notifications to the user for a given project", response = Json.class)
  @GetMapping("/api/v1/notifications/{projectId}/{userId}/{status}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Return the list of all the available notifications"),
        @ApiResponse(code = 400, message = "Failed to return the notifications")
      })
  public ResponseEntity<Object> getNotifications(@PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "userId") final int userId, @PathVariable(value = "status") final String status) {
    Map<String, Object> response;
    try {
      dbUtil.changeSchema("public");
      if(NotificationStatus.UNREAD.getStatus().equals(status)) {
        List<Notification> notificationList = auditEventsService.getAllNewNotifications(projectId, userId) ;
        auditEventsService.setNotificationAsRead(notificationList);
        response = createSuccessMessage(HttpStatus.SC_OK, "Successful", notificationList);
      } else if(NotificationStatus.ALL.getStatus().equals(status)) {
        List<Notification> notificationList = auditEventsService.getAllNotifications(projectId, userId) ;
        response = createSuccessMessage(HttpStatus.SC_OK, "Successful", notificationList);
      } else if(NotificationStatus.COUNT.getStatus().equals(status)) {
        Integer count = auditEventsService.getNotificationCounts(projectId, userId);
        response = createSuccessMessage(HttpStatus.SC_OK, "Successful", count);
      } else {
        response = failureMessage(HttpStatus.SC_BAD_REQUEST, "One of the parameters is missing or incorrect. Expected parameters are projectId, userId, unread|all|count");
      }
    } catch (final Exception ex) {
      response = failureMessage(HttpStatus.SC_BAD_REQUEST, ex.getMessage());
    }
    ResponseEntity<Object> responseEntity = null;
    if(HttpStatus.SC_BAD_REQUEST == Integer.valueOf((Integer)response.get("code"))) {
      responseEntity = ResponseEntity.badRequest().body(response);
    } else {
      responseEntity = ResponseEntity.ok().body(response);
    }
    return responseEntity;
  }
  
  @ApiOperation(value = "Update the notification as inactive", response = Json.class)
  @PutMapping("/api/v1/notifications/{notificationId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Notification is inactive"),
        @ApiResponse(code = 400, message = "Notification not found")
      })
  public ResponseEntity<Object> updateNotification(@PathVariable(value = "notificationId") final int notificationId) {
    Map<String, Object> response;
    try {
      dbUtil.changeSchema("public");
      Notification notification = auditEventsService.update(notificationId);
      if(notification != null) {
        response = createSuccessMessage(HttpStatus.SC_OK, "Notification is inactive", notification);
      } else {
        response = failureMessage(HttpStatus.SC_BAD_REQUEST, "Notification not found");
      }
    } catch (final Exception ex) {
      response = failureMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
    }
    return ResponseEntity.ok().body(response);
  }
  
  private HashMap<String, Object> failureMessage(final int code, final String message) {
    final HashMap<String, Object> success = new HashMap<>();
    success.put("code", code);
    success.put("message", message);
    return success;
  }

  private HashMap<String, Object> createSuccessMessage(
      final int code, final String message, Object result) {
    final HashMap<String, Object> success = new HashMap<>();
    success.put("code", code);
    success.put("result", result);
    success.put("message", message);
    return success;
  }
}
