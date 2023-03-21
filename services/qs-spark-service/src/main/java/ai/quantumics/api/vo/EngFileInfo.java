package ai.quantumics.api.vo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import ai.quantumics.api.req.Metadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EngFileInfo extends FileInfo {
  
  @JsonProperty private int fileId;
  @JsonProperty private int folderId;
  @JsonProperty private String category;
  
  @JsonProperty private List<Metadata> metadata;
}
