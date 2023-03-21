package ai.quantumics.api.req;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DatabaseFileRequest {
	
	@JsonProperty("databaseObjectRequest") private List<UploadFileRequest> databaseObjectRequest;

}
