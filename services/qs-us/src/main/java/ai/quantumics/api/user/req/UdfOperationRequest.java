package ai.quantumics.api.user.req;


import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UdfOperationRequest {
  private int projectId;
  private int engFlowId;
  //private int eventId;
  private Map<String, Integer> eventIds;
  private int autoConfigEventId;
  // private String uniqueId;
  private String udfName;
  private String fileType;
  // private int firstFileId;
  // private int secondFileId;
  //private int eventId1;
  //private int eventId2;
  private String dfId;
  // private String joinOperations; Service B will update it already
  //@NotBlank private String secondFileColumn;
  //@NotBlank private String firstFileColumn;
  private List<UdfReqColumn> arguments;
  private int fileId;
  private String udfFunction;
  
  
  private FileJoinColumnMetadata metadata;
}