/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai. Morbi non lorem porttitor neque
 * feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan. Etiam sed turpis ac ipsum condimentum
 * fringilla. Maecenas magna. Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque
 * sagittis ligula eget metus. Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.qs.api.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "qsp_eng_flow_event")
public class EngFlowEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int engFlowEventId;

  private int autoConfigEventId;

  @Column(columnDefinition = "TEXT")
  private String engFlowEventData;

  @Column(columnDefinition = "TEXT")
  private String engFlowConfig;

  private String engFlowS3Location;

  @Column(columnDefinition = "TEXT")
  private String fileMetaData;

  @Column(columnDefinition = "TEXT")
  private String joinOperations;

  private String eventType;
  private int engFlowId;
  private boolean eventStatus;
  private double eventProgress;
  
  private String livyStmtExecutionStatus;
  private String livyStmtOutput;
  private Date livyStmtStartTime;
  private Date livyStmtEndTime;
  private long livyStmtDuration;
  
  private int projectId;
  private int folderId;
  private int fileId;
  private int userId;
  private Date creationDate;
  
  private String fileType;
  private boolean deleted;
}
