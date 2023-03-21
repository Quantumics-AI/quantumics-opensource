package ai.quantumics.api.util;

public abstract class CleansingRuleEngine {
  
  protected abstract String getRuleDefinition(String ruleName, String subRuleName, Object... args);
  
  protected String getRuleDefinition(String ruleName, String subRuleName, String option, Object... args) {
    return null;
  }
  
}
