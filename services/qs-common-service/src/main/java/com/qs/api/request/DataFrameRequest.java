package com.qs.api.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataFrameRequest {
  @JsonProperty("uniqueId") private String uniqueId;
  @JsonProperty("projectId") private int projectId;
  @JsonProperty("folderId") private int folderId;
  @JsonProperty("fileId") private int fileId;
  @JsonProperty("engFlowId") private int engFlowId;
  @JsonProperty("eventId") private int eventId;
  @JsonProperty("autoConfigEventId") private int autoConfigEventId;
  @JsonProperty("name") private String name;
  @JsonProperty("engFlowName") private String engFlowName;
  @JsonProperty("fileType") private String fileType;
  //@JsonProperty("type") private String type;
  @JsonProperty("userId") private int userId;
  @JsonProperty("metadata") private FileJoinColumnMetadata metadata;
    
}
