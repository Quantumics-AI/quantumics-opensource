package ai.quantumics.api.service.impl;

import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.exceptions.BadRequestException;
import ai.quantumics.api.model.AuditEvents;
import ai.quantumics.api.repo.AuditEventsRepository;
import ai.quantumics.api.service.AuditEventsService;
import ai.quantumics.api.vo.AuditEventResponse;
import ai.quantumics.api.vo.Notification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuditEventsServiceImpl implements AuditEventsService {
  
  private AuditEventsRepository auditEventsRepository;

  /**
   * @param auditEventsRepository
   */
  public AuditEventsServiceImpl(AuditEventsRepository auditEventsRepository) {
    this.auditEventsRepository = auditEventsRepository;
  }

  @Override
  public void saveAuditEvents(AuditEvents auditEvents) {
    auditEventsRepository.saveAndFlush(auditEvents);

  }

  @Override
  public List<AuditEventResponse> getAuditEvents(Integer projectId, String subscriptionType, String startDate, String endDate) throws SQLException, ParseException {
      List<AuditEvents> auditEventsModel = new ArrayList<>();
      if((StringUtils.isNotEmpty(startDate) && StringUtils.isEmpty(endDate)) || (StringUtils.isEmpty(startDate) && StringUtils.isNotEmpty(endDate))) {
          throw new BadRequestException("Both StartDate and EndDate is required ");
      }
      if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate)) {
          Date startDateUtc = QsConstants.getUTCDate(startDate);
          Date endDateUtc = QsConstants.getUTCDate(endDate);
          if(startDateUtc.after(endDateUtc)) {
              throw new BadRequestException("StartDate should be before than EndDate");
          }
          auditEventsModel = auditEventsRepository.findByProjectIdAndActiveTrueAndCreationDateBetweenOrderByAuditIdDesc(projectId, startDateUtc, endDateUtc);
      } else {
          auditEventsModel = auditEventsRepository.findByProjectIdAndActiveTrueOrderByAuditIdDesc(projectId);
      }
      List<AuditEventResponse> auditEventResponseList = new ArrayList<>();

      int count = auditEventsModel.size();
      if (QsConstants.SUBSCRIPTION_TYPE_DEFAULT.equals(subscriptionType)) {
          count = 20;
      }

      auditEventsModel.stream().limit(count).forEach(
              (auditEvents) -> {
                  AuditEventResponse auditEventResponse = new AuditEventResponse();
                  auditEventResponse.setAuditId(auditEvents.getAuditId());
                  auditEventResponse.setEventType(auditEvents.getEventType());
                  auditEventResponse.setEventTypeAction(auditEvents.getEventTypeAction());
                  auditEventResponse.setAuditMessage(auditEvents.getAuditMessage());
                  auditEventResponse.setCreationDate(auditEvents.getCreationDate());
                  auditEventResponse.setUserName(auditEvents.getUserName());
                  auditEventResponse.setUserId(auditEvents.getUserId());
                  auditEventResponseList.add(auditEventResponse);
              }
      );
      return auditEventResponseList;
  }

  @Override
  public List<Notification> getAllNotifications(Integer projectId, Integer userId)
      throws SQLException {
    List<AuditEvents> auditEventsModel = auditEventsRepository.findByProjectIdAndActiveTrueAndUserIdAndIsNotifyTrueOrderByAuditIdDesc(projectId, userId);
    List<Notification> notificationList = new ArrayList<>();
    auditEventsModel.stream().forEach(
        (auditEvents) -> {
          Notification notification = buildNotification(auditEvents);
          notificationList.add(notification);
        }
    );
     return notificationList;
  }

  @Override
  public List<Notification> getAllNewNotifications(Integer projectId, Integer userId)
      throws SQLException {
    List<AuditEvents> auditEventsModel = auditEventsRepository.findByProjectIdAndActiveTrueAndUserIdAndIsNotifyTrueAndIsNotifyReadOrderByAuditIdDesc(projectId, userId, false);
    List<Notification> notificationList = new ArrayList<>();
    auditEventsModel.stream().forEach(
        (auditEvents) -> {
          Notification notification = buildNotification(auditEvents);
          notificationList.add(notification);
        }
    );
     return notificationList;
  }

  @Override
  public Integer getNotificationCounts(Integer projectId, Integer userId) throws SQLException {
    return auditEventsRepository.countByProjectIdAndActiveTrueAndUserIdAndIsNotifyTrueAndIsNotifyReadFalse(projectId, userId);
  }

  @Override
  public void setNotificationAsRead(List<Notification> notificationList) throws SQLException {
    List<AuditEvents> auditEventsModel  = new ArrayList<>();
    List<Integer> notificationIds = 
    notificationList.stream().map((notification) -> notification.getNotificationId()).collect(Collectors.toList());
    auditEventsModel = auditEventsRepository.findAllById(notificationIds);
    auditEventsModel.stream().forEach( (auditEvents) -> {
      auditEvents.setNotifyRead(true);
    });
    
    auditEventsRepository.saveAll(auditEventsModel);
    
    if(log.isInfoEnabled()) {
      log.info(String.format("Notifications(%1$d) Marked as read.", auditEventsModel.size()));
    }
  }
  
  @Override
  public Notification update(int notificationId) throws SQLException {
    Optional<AuditEvents> auditEventsModel = auditEventsRepository.findById(notificationId);
    Notification notification = null;
    if(auditEventsModel.isPresent()) {
      auditEventsModel.get().setActive(false);
      AuditEvents auditEvents = auditEventsRepository.saveAndFlush(auditEventsModel.get());
      notification = buildNotification(auditEvents);
    }
    return notification;
  }
  
  /**
   * @param auditEvents
   * @return
   */
  private Notification buildNotification(AuditEvents auditEvents) {
    Notification notification = new Notification();
    notification.setNotificationId(auditEvents.getAuditId());
    notification.setEventType(auditEvents.getEventType());
    notification.setEventTypeAction(auditEvents.getEventTypeAction());
    notification.setNotificationMsg(auditEvents.getNotificationMessage());
    notification.setCreationDate(auditEvents.getCreationDate());
    notification.setUserName(auditEvents.getUserName());
    notification.setNotificationRead(auditEvents.isNotifyRead());
    notification.setActive(auditEvents.isActive());
    return notification;
  }

}
