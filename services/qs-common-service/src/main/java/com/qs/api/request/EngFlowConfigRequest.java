package com.qs.api.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EngFlowConfigRequest {
  @JsonProperty("projectId") private int projectId;
  @JsonProperty("userId") private int userId;
  @JsonProperty("engineeringId") private int engineeringId;
  @JsonProperty("selectedFiles") private List<DataFrameRequest> selectedFiles;
  @JsonProperty("selectedJoins") private List<JoinOperationRequest> selectedJoins;
  @JsonProperty("selectedAggregates") private List<AggregateRequest> selectedAggregates;
  @JsonProperty("selectedUdfs") private List<UdfOperationRequest> selectedUdfs;
}
