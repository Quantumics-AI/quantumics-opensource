package ai.quantumics.api.util;

import java.util.HashMap;
import java.util.Map;

public class FormatRuleEngine extends CleansingRuleEngine {
  
  private static Map<String, String> formatRules = new HashMap<>();
  
  static {
    formatRules.put("formatRemoveWhitespaces", "df = removeTrim('%s')\n");
    formatRules.put("formatRemoveAscents", "df = removeAscents('%s')\n");
    formatRules.put("formatPadWithLeadingCharacters", "df = padWithLeadingCharacters('%s', %d, '%s')\n");
    formatRules.put("formatRemoveSpecialCharacters", "df = removeSpecialCharacters('%s')\n");
    formatRules.put("formatTrimQuotes", "df = trimQuotes('%s')\n");
    formatRules.put("formatTrimWhitespace", "df = trimWhitespaces('%s')\n");
    formatRules.put("formatUpper", "df = formatUpper('%s')\n");
    formatRules.put("formatLower", "df = formatLower('%s')\n");
    formatRules.put("formatProper", "df = formatProper('%s')\n");
    formatRules.put("formatAddPrefix", "df = addPrefix('%s', '%s')\n");
    formatRules.put("formatAddSuffix", "df = addSuffix('%s', '%s')\n");
  }
  

  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, Object... args) {
    String ruleDefFormat = formatRules.get(ruleName+subRuleName);
    
    return QsUtil.setRuleArgs(ruleDefFormat, args);
  }

}
