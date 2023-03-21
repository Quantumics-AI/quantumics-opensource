package com.qs.api.vo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@JsonPropertyOrder({
  "projectId",
  "rawFilesResponse",
  "cleansedFilesResponse",
  "engineeredFilesResponse"
})
public class EngFlowEventFiles {
  
  @JsonProperty private int projectId;

  @JsonProperty private List<FolderDetails> rawFilesResponse;

  @JsonProperty private List<FolderDetails> cleansedFilesResponse;

  @JsonProperty private List<EngFlowDetails> engineeredFilesResponse;

}
