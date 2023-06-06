package ai.quantumics.api.vo;

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
		"yesterdayCount",
		"averageRange",
		"summary"
})
public class DataVolumeOverview {
	@JsonProperty
	private double total;

	@JsonProperty
	private String unit;

	@JsonProperty
	private double yesterdayCount;

	@JsonProperty
	private String averageRange;

	@JsonProperty
	private Map<String, ? extends Object> summary;

}
