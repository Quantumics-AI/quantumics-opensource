package ai.quantumics.api.util;

import java.util.HashMap;
import java.util.Map;

public class FillNullRuleEngine extends CleansingRuleEngine {
  
  private static Map<String, String> fillNullRules = new HashMap<>();
  
  static {
    fillNullRules.put("fillNullWithCustomValue", "df = missingWithCustomValues('%s', '%s')\n");
    fillNullRules.put("fillNullWithLastValue", "df = missingWithLatestValues('%s')\n");
    fillNullRules.put("fillNullWithNullValue", "df = missingWithNullValues('%s')\n");
    fillNullRules.put("fillNullWithAverageValue", "df = missingWithAverageValues('%s')\n");
    fillNullRules.put("fillNullWithSumValue", "df = missingWithSumValues('%s')\n");
    fillNullRules.put("fillNullWithModValue", "df = missingWithModValues('%s')\n");
  }

  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, Object... args) {
    String ruleDefFormat = fillNullRules.get(ruleName+subRuleName);
    
    return QsUtil.setRuleArgs(ruleDefFormat, args);
  }

}
