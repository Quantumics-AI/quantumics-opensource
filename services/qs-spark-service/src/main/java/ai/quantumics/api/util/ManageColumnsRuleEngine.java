package ai.quantumics.api.util;

import java.util.HashMap;
import java.util.Map;

public class ManageColumnsRuleEngine extends CleansingRuleEngine {
  
  private static Map<String, String> manageColsRules = new HashMap<>();

  static {
    manageColsRules.put("manageColumnsCreate", "df = createColumn('%s', '%s', '%s')\n");
    manageColsRules.put("manageColumnsDrop", "df = dropColumn('%s')\n");
    manageColsRules.put("manageColumnsClone", "df = cloneColumn('%s')\n");
    manageColsRules.put("manageColumnsRename", "df = renameColumn('%s', '%s')\n");
  }
  
  @Override
  protected String getRuleDefinition(String ruleName, String subRuleName, Object... args) {
    String ruleDefFormat = manageColsRules.get(ruleName+subRuleName);
    
    return QsUtil.setRuleArgs(ruleDefFormat, args);
  }

}
