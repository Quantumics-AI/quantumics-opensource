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
  "summary"
})
public class StatisticsOverview {
	@JsonProperty
	private long total;
	
	@JsonProperty
	private Map<String, ? extends Object> summary;

}
