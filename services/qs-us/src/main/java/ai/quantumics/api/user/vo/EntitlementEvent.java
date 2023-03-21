package ai.quantumics.api.user.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntitlementEvent {
	private String action;
	@JsonProperty("customer-identifier")
	private String customerIdentifier;
	@JsonProperty("product-code")
	private String productCode;
}
