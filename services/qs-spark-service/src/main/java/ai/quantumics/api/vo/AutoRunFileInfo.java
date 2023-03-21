package ai.quantumics.api.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"folderId", "folderName", "folderDisplayName", "fileId", "fileName", "creationDate", "engFlowId", "engFlowName", "flowCreationDate"})
@EqualsAndHashCode
public class AutoRunFileInfo {
  
  private int eventId;
  private int folderId;
  private String folderName;
  private String folderDisplayName;
  private int fileId;
  private String fileName;
  private Date creationDate;
  private int engFlowId;
  private String engFlowName;
  private Date flowCreationDate;
}
