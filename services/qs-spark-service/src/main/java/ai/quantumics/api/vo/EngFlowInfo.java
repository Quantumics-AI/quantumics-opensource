package ai.quantumics.api.vo;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EngFlowInfo {

	@JsonProperty
	@NotNull(message = "Project id required")
	private Integer projectId;
	@JsonProperty
	@NotNull(message = "User id required")
	private Integer userId;
	@JsonProperty
	@NotNull(message = "Eng flow id required")
    private Integer engFlowId;
	@JsonProperty
	@NotEmpty(message = "Eng flow name required")
	private String engFlowName;
	private String engFlowDesc;
}
