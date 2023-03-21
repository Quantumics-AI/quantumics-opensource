package ai.quantumics.api.util;

import java.util.HashMap;
import java.util.Map;

public class SplitRuleEngine extends CleansingRuleEngine {
  
  private static Map<String, String> splitRules = new HashMap<>();
  
  static {
    splitRules.put("splitByDelimiter", "df = splitByDelimiter('%s', '%s', '%s')\n");
    splitRules.put("splitBetweenTwoDelimiters", "df = splitBetweenTwoDelimiters('%s', '%s', '%s', '%s')\n");
    splitRules.put("splitByMultipleDelimiters", "df = splitByMultipleDelimiters('%s', '%s', '%s')\n");
    splitRules.put("splitAtPositions", "df = splitAtPositions('%s', '%s', '%s')\n");
    splitRules.put("splitByRegularInterval", "df = splitByRegularInterval('%s', '%s', '%s', '%s', '%s')\n");
    splitRules.put("splitBetweenTwoPositions", "df = splitBetweenTwoPositions('%s', %d, %d, '%s')\n");
  }

  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, Object... args) {
    String ruleDefFormat = splitRules.get(ruleName+subRuleName);
    
    return QsUtil.setRuleArgs(ruleDefFormat, args);
  }

}
