package ai.quantumics.api.util;

import ai.quantumics.api.constants.QsConstants;

public class CleansingRuleEngineFactory {
  
  public CleansingRuleEngine getInstance(String ruleType) {
    if(QsConstants.COUNT_MATCH.equals(ruleType)) {
      return new CountMatchRuleEngine();
    }
    else if(QsConstants.FILL_NULL.equals(ruleType)) {
      return new FillNullRuleEngine();
    }
    else if(QsConstants.SPLIT.equals(ruleType)) {
      return new SplitRuleEngine();
    }
    else if(QsConstants.EXTRACT_COLUMN_VALUES.equals(ruleType)) {
      return new ExtractColumnValuesEngine();
    }
    else if(QsConstants.FORMAT.equals(ruleType)) {
      return new FormatRuleEngine();
    }
    else if(QsConstants.MANAGE_COLUMNS.equals(ruleType)) {
      return new ManageColumnsRuleEngine();
    }
    else if(QsConstants.FILTER_ROWS.equals(ruleType)) {
      return new FilterRowsRuleEngine();
    }
    else if(QsConstants.FILTER_ROWS_BY_COLUMN.equals(ruleType)) {
      return new FilterRowsByColumnRuleEngine();
    }
    else if(QsConstants.UNCATEGORIZED.equals(ruleType)) {
      return new UncategorizedRuleEngine();
    }
    
    return null;
  }
}
