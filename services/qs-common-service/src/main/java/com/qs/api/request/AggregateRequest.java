package com.qs.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonPropertyOrder({"projectId","engFlowId", "eventId", "autoConfigEventId", "dataFrameEventId", "groupByColumns", "columns","groupByColumnsMetaData", "columnsMetaData"})
public class AggregateRequest {
  
  @JsonProperty("projectId")
  private int projectId;
  
  @JsonProperty("userId") 
  private int userId;
  
  @JsonProperty("engFlowId")
  private int engFlowId;

  @JsonProperty("eventId")
  private int eventId;
  
  @JsonProperty("autoConfigEventId") 
  private int autoConfigEventId;
  
  @JsonProperty("dataFrameEventId")
  private int dataFrameEventId;

  @JsonProperty("groupByColumns")
  private List<String> groupByColumns;

  @JsonProperty("columns")
  private List<AggReqColumn> columns;

  @JsonProperty("groupByColumnsMetaData")
  private List<Metadata> groupByColumnsMetaData;
  
  @JsonProperty("columnsMetaData")
  private List<AggReqColumn> columnsMetaData;
}
