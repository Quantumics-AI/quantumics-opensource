package com.qs.api.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinOperationRequest {
  @JsonProperty("projectId") private int projectId;
  @JsonProperty("userId") private int userId;
  @JsonProperty("engFlowId") private int engFlowId;
  @JsonProperty("autoConfigEventId") private int autoConfigEventId;
  @JsonProperty("eventId") private int eventId;
  @JsonProperty("eventId1") private int eventId1;
  @JsonProperty("eventId2") private int eventId2;
  @JsonProperty("dfId") private String dfId;
  @JsonProperty("uniqueId") private String uniqueId;
  @JsonProperty("joinName") private String joinName;
  @JsonProperty("joinType") private String joinType;
  @JsonProperty("joinOperations") private String joinOperations;
  @JsonProperty("firstFileName") private String firstFileName;
  @JsonProperty("secondFileName") private String secondFileName;
    @NotBlank
    @JsonProperty("firstFileColumn") private String firstFileColumn;
    @NotBlank
    @JsonProperty("secondFileColumn") private String secondFileColumn;

    @JsonProperty("metadata") private List<ColumnMetaData> metadata;
}
