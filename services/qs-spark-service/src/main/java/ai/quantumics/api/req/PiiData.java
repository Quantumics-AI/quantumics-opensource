package ai.quantumics.api.req;

import lombok.Data;

@Data
public class PiiData {
	  private String dropColumns;
	  private String encryptPiiColumns;
	  private int folderId;
}

