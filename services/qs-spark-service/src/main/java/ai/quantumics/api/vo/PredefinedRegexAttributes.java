package ai.quantumics.api.vo;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class PredefinedRegexAttributes {
	private String attributeId;
	private String attributeType;
	@EqualsAndHashCode.Exclude
	private Map<String, String> regexDictionary;
}
