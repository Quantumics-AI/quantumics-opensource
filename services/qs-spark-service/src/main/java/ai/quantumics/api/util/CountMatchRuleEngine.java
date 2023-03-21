package ai.quantumics.api.util;

import java.util.HashMap;
import java.util.Map;

public class CountMatchRuleEngine extends CleansingRuleEngine {
  
  private static Map<String, String> countMatchRules = new HashMap<>();
  
  static {
    countMatchRules.put("countMatchText", "df=countByCharacter('%s','%s','%s')\n"); 
    countMatchRules.put("countMatchDelimiter", "df=countByDelimiter('%s','%s','%s','%s')\n");
  }

  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, Object... args) {
    String ruleDefFormat = countMatchRules.get(ruleName+subRuleName);
    
    return QsUtil.setRuleArgs(ruleDefFormat, args);
  }
}
