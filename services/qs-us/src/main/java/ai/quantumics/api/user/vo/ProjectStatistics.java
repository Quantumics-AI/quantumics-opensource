package ai.quantumics.api.user.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonPropertyOrder({
  "requestType", "sourceDataset", "preparedDataset", "dataPipes", "dataVolume", "pii", "savings"
})
public class ProjectStatistics {
	
	@JsonProperty
	private String requestType;
	
	@JsonProperty
	private StatisticsOverview sourceDataset;
	
	@JsonProperty
	private StatisticsOverview preparedDataset;
	
	@JsonProperty
	private StatisticsOverview dataPipes;
	
	@JsonProperty
	private DataVolumeOverview dataVolume;
	
	@JsonProperty
	private StatisticsOverview pii;
	
	@JsonProperty
	private Savings savings;
	
	@Data
	@NoArgsConstructor
	public class Savings {
		private Effort effort;
		private ValueUnitPair time;
		private ValueUnitPair money;
	}
	
	@Data
	@NoArgsConstructor
	public class Effort {
		private int manual;
		private int qsai;
		private String unit;
	}
	
	@Data
	@NoArgsConstructor
	public class ValueUnitPair{
		private double value;
		private String unit;
	}
}
