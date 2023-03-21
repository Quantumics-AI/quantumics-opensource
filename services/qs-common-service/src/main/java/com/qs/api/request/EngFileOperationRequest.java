package com.qs.api.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EngFileOperationRequest {
  private int projectId;
  private int userId;
  private int fileId;
  private int engFlowId;
  private String engFlowName;
  private int parentEventId;
  private int eventId;
  private int autoConfigEventId;
  private String fileType;
}
