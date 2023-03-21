package ai.quantumics.api.util;

import java.util.HashMap;
import java.util.Map;

public class UncategorizedRuleEngine extends CleansingRuleEngine {
  
  private static Map<String, String> uncatRules = new HashMap<>();
  
  static {
    uncatRules.put("findReplace", "df = replaceCells('%s', '%s', '%s')\n");
    uncatRules.put("patternReplace", "df = replaceTextOrPattern('%s', re.escape('%s'), re.escape('%s'))\n");
    uncatRules.put("replace cell", "df = replaceCells('%s', '%s', '%s')\n"); // Unused
    uncatRules.put("groupBysum", "df = groupBysum('%s', '%s')\n");
    uncatRules.put("groupBymax", "df = groupBymax('%s', '%s')\n");
    uncatRules.put("groupBymin", "df = groupBymin('%s', '%s')\n");
    uncatRules.put("groupByavg", "df = groupByavg('%s', '%s')\n");
    uncatRules.put("standardize", "df = standardize('%s', '%s', '%s')\n");
    uncatRules.put("removeDuplicateRows", "df = removeDuplicateRows()\n");
  }

  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, Object... args) {
    String ruleDefFormat = uncatRules.get(ruleName+subRuleName);
    
    return QsUtil.setRuleArgs(ruleDefFormat, args);
  }

}
