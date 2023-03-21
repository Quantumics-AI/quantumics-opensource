package com.qs.api.request;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UdfOperationRequest {
	
	 @JsonProperty("projectId") private int projectId;
	  @JsonProperty("userId") private int userId;
	  @JsonProperty("engFlowId") private int engFlowId;
	  @JsonProperty("autoConfigEventId") private int autoConfigEventId;
	  //@JsonProperty("eventId") private int eventId;
	  //@JsonProperty("eventId1") private int eventId1;
	  //@JsonProperty("eventId2") private int eventId2;
	  @JsonProperty("eventIds") private Map<String, Integer> eventIds;
	  @JsonProperty("dfId") private String dfId;
	  @JsonProperty("uniqueId") private String uniqueId;
	  @JsonProperty("fileType") private String fileType;
	  @JsonProperty("joinOperations") private String joinOperations;
	  @JsonProperty("firstFileName") private String firstFileName;
	  @JsonProperty("secondFileName") private String secondFileName;
	  @JsonProperty("metadata") private List<ColumnMetaData> metadata;
	  @JsonProperty("arguments") private List<UdfReqColumn> arguments;
	  @JsonProperty("fileId") private int fileId;
	  @JsonProperty("udfFunction") private String udfFunction;

}
