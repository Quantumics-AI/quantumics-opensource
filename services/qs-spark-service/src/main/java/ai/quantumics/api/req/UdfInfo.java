package ai.quantumics.api.req;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonPropertyOrder({"udfId", "projectId", "userId", "udfName", "udfScript", "udfSyntax", "udfFilePath", "udfScriptLanguage", "udfIconPath", "arguments", "udfVersion", "active", "createdDate","modifiedDate","createdBy","modifiedBy", "udfReturnvalue", "udfPublish"})
public class UdfInfo {
	private int udfId;
	private int projectId;
	private int userId;
	
	private String udfName;
    private String udfScript;
    private String udfSyntax;
	private String udfFilePath;
	private String udfScriptLanguage;
	private String udfIconPath;
	
	private List<UdfReqColumn> arguments;
	private String udfVersion;
	
	private boolean active;
	private Date createdDate;
	private Date modifiedDate;
	private String createdBy;
	private String modifiedBy;
	private String udfReturnvalue;
	private boolean udfPublish;
	
}
