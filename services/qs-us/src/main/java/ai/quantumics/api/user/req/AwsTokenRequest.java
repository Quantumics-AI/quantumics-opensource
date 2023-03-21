package ai.quantumics.api.user.req;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AwsTokenRequest {

	@JsonProperty("x-amzn-marketplace-token")
	private String xAmznMarketplaceToken;
	
}
