package ai.quantumics.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"fileId","source","target","category","metadata","fileType","rules"})
public class DataLineageInfo {
  
  @JsonProperty("fileId")
  private String fileId;
  
  @JsonProperty("source")
  private String source;
  
  @JsonProperty("target")
  private String target;
  
  @JsonProperty("category")
  private String category;
  
  @JsonProperty("metadata")
  private String metadata;
  
  @JsonProperty("fileType")
  private String fileType;
  
  @JsonProperty("rules")
  private String rules;
  
}
