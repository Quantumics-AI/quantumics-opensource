package ai.quantumics.api.user.vo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonPropertyOrder({
  "total",
  "unit",
  "summary"
})
public class DataVolumeOverview {
	@JsonProperty
	private double total;
	
	@JsonProperty
	private String unit;
	
	@JsonProperty
	private Map<String, ? extends Object> summary;

}
