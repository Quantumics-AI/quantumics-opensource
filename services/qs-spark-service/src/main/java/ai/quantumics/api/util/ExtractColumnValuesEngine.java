package ai.quantumics.api.util;

import java.util.HashMap;
import java.util.Map;

public class ExtractColumnValuesEngine extends CleansingRuleEngine {
  
  private static Map<String, String> extractColValRules = new HashMap<>();
  
  static {
    extractColValRules.put("extractColumnValuesFirstNChars", "df = extractFirstNCharacters('%s', '%s', '%s')\n");
    extractColValRules.put("extractColumnValuesLastNChars", "df = extractLastNCharacters('%s', '%s', '%s')\n");
    extractColValRules.put("extractColumnValuesTypeMismatched", "df = typeMismatched('%s', '%s', '%s')\n");
    extractColValRules.put("extractColumnValuesCharsInBetween", "df = charactersBetweenTwoPositions('%s', %d, %d, '%s')\n");
    extractColValRules.put("extractColumnValuesQueryString", "df = extractQueryString('%s', '%s', '%s')\n");
    extractColValRules.put("extractColumnValuesBetweenTwoDelimiters", "df = extractBetweenTwoDelimiters('%s', '%s', '%s', %d, '%s')\n");
    extractColValRules.put("extractColumnValuesTextOrPattern", "df = extractTextOrPattern('%s', '%s', '%s', '%s', %d, '%s')\n");
    extractColValRules.put("extractColumnValuesNumbers", "df = extractNumber('%s', %d, '%s')\n");
  }

  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, Object... args) {
    String ruleDefFormat = extractColValRules.get(ruleName+subRuleName);
    
    return QsUtil.setRuleArgs(ruleDefFormat, args);
  }

}
