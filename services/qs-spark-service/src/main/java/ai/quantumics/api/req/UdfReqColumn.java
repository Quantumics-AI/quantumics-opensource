package ai.quantumics.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonRootName("Column")
public class UdfReqColumn {
	@JsonProperty private String dataValue;
	@JsonProperty private String options;
	@JsonProperty private String dataType;
}
