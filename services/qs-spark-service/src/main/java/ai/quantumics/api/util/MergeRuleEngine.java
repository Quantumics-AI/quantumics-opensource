package ai.quantumics.api.util;

import java.util.HashMap;
import java.util.Map;

public class MergeRuleEngine extends CleansingRuleEngine {
  
  private static Map<String, String> mergeRules = new HashMap<>();
  
  static {
    mergeRules.put("mergeRule", "df = mergeRule('%s', '%s', '%s')\n");
  }

  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, Object... args) {
    String ruleDefFormat = mergeRules.get(ruleName+subRuleName);
    
    return QsUtil.setRuleArgs(ruleDefFormat, args);
  }

}
