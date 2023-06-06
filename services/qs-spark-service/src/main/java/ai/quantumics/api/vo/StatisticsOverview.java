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
		"yesterdayCount",
		"averageRange",
		"summary"
})
public class StatisticsOverview {
	@JsonProperty
	private long total;

	@JsonProperty
	private long yesterdayCount;

	@JsonProperty
	private String averageRange;

	@JsonProperty
	private Map<String, ? extends Object> summary;

}
