package ai.quantumics.api.util;

import java.util.HashMap;
import java.util.Map;

public class FilterRowsByColumnRuleEngine extends CleansingRuleEngine {
  
  private static Map<String, String> filterRowsByColumnRules = new HashMap<>();
  
  static {
    filterRowsByColumnRules.put("filterRowsByColumnMissing", "df = isMissing('%s', '%s')\n");
    filterRowsByColumnRules.put("filterRowsByColumnExactly", "df = isExactly('%s', '%s', '%s')\n");
    filterRowsByColumnRules.put("filterRowsByColumnNotEqualTo", "df = notEqualTo('%s', '%s', '%s')\n");
    filterRowsByColumnRules.put("filterRowsByColumnValueIsOneOf", "df = columnValueIsOneOf('%s', '%s', '%s')\n");
    filterRowsByColumnRules.put("filterRowsByColumnLessThanOrEqualTo", "df = lessThanOrEqualTo('%s', '%s', '%s')\n");
    filterRowsByColumnRules.put("filterRowsByColumnGreaterThanOrEqualTo", "df = greaterThanOrEqualTo('%s', '%s', '%s')\n");
    filterRowsByColumnRules.put("filterRowsByColumnBetween", "df = isBetween('%s', '%s', '%s', '%s')\n");
    filterRowsByColumnRules.put("filterRowsByColumnContains", "df = contains('%s', '%s', '%s')\n");
    filterRowsByColumnRules.put("filterRowsByColumnStartsWith", "df = startsWith('%s', '%s', '%s')\n");
    filterRowsByColumnRules.put("filterRowsByColumnEndsWith", "df = endsWith('%s', '%s', '%s')\n");
  }

  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, Object... args) {
    String ruleDefFormat = filterRowsByColumnRules.get(ruleName+subRuleName);
    
    return QsUtil.setRuleArgs(ruleDefFormat, args);
  }

}
