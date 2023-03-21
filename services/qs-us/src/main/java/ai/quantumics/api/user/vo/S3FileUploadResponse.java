package ai.quantumics.api.user.vo;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3FileUploadResponse {
	
	private String s3FileUrl;
	private String fileName;
	private String fileSize;
	private String fileContentType;
	private Map<String, String> columnMetaData;
	
}
