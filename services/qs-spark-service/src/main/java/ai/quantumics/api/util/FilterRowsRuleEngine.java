package ai.quantumics.api.util;

import java.util.HashMap;
import java.util.Map;

public class FilterRowsRuleEngine extends CleansingRuleEngine {
  
  private static Map<String, String> filterRowsRules = new HashMap<>();
  
  static {
    filterRowsRules.put("filterRowsTopRows", "df = topRows(%d, '%s')\n");
    filterRowsRules.put("filterRowsTopRowsAtRegularInterval", "df = topRowsAtRegularIntervalByDefault(%d, %d, '%s')\n");
    filterRowsRules.put("filterRowsRangeRows", "df = rangeRows(%d, %d, '%s')\n");
    
    filterRowsRules.put("filterRowsTopRowsByColumn", "df = topRowsByColumn(%d, '%s', '%s')\n");
    filterRowsRules.put("filterRowsTopRowsAtRegularIntervalByColumn", "df = topRowsAtRegularInterval('%s', %d, %d, '%s')\n");
    filterRowsRules.put("filterRowsRangeRowsByColumn", "df = rangeRowsByColumn(%d, %d, '%s', '%s')\n");
  }
  
  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, Object... args) {
    return null;
  }
  
  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, String option, Object... args) {
    String ruleDefFormat = filterRowsRules.get(ruleName+subRuleName+option); // option argument will have value either "ByColumn" or ""
    
    return QsUtil.setRuleArgs(ruleDefFormat, args);
    
  }

}
