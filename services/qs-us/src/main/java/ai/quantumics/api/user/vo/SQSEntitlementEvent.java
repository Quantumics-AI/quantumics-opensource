package ai.quantumics.api.user.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SQSEntitlementEvent {
	@JsonProperty("Type")
	private String type;
	@JsonProperty("MessageId")
	private String messageId;
	@JsonProperty("TopicArn")
	private String topicArn;
	@JsonProperty("Message")
	private String message;
	@JsonProperty("Timestamp")
	private Date timestamp;
	@JsonProperty("SignatureVersion")
	private String signatureVersion;
	@JsonProperty("Signature")
	private String signature;
	@JsonProperty("SigningCertURL")
	private String signingCertURL;
	@JsonProperty("UnsubscribeURL")
	private String unsubscribeURL;
}
