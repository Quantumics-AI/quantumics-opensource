package ai.quantumics.api.helper;

import org.springframework.stereotype.Component;

@Component
public class RunJobHelper {
  
  final String comma = ",";
  final String singleQuote = "'";
  
  public void commonMethod(
      StringBuilder cleanseRuleInvocation, String ruleInputValues, String ruleImpactedCols2) {
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  // Find and Replace rules related helper method.
  
  public void qsPatternReplace(
      StringBuilder cleanseRuleInvocation,
      String ruleInputValues,
      String ruleInputValues1,
      String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = patternReplace(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }

  public void qsFindReplace(
      StringBuilder cleanseRuleInvocation,
      String ruleInputValues,
      String ruleInputValues1) {
    cleanseRuleInvocation.append("df = findReplace(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  // Merge rule related helper method.
  
  public void qsMergeRule(
      StringBuilder cleanseRuleInvocation,
      String ruleDelimiter,
      String ruleInputValues,
      String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = mergeRule(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2); // lower case new column name
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues.toLowerCase()); // lower case new column name
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleDelimiter);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  // Standardize rule related helper method.
  
  public void qsStandardize(
      StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1) {
    cleanseRuleInvocation.append("df = standardize(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  // Remove duplicates related helper method.
  
  public void qsRemoveDuplicates(StringBuilder cleanseRuleInvocation){
    cleanseRuleInvocation.append("df = removeDuplicateRows()");
  }
  
  // Split Rules related helper methods.
  
  public void qsSplitByDelimiter(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = splitByDelimiter(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsSplitBetweenTwoDelimeters(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = splitBetweenTwoDelimiters(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsSplitByMultipleDelimeters(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = splitByMultipleDelimiters(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsSplitAtPositions(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = splitAtPositions(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsSplitByRegularInterval(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1, String ruleInputValues2, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = splitByRegularInterval(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsSplitBetweenTwoPositions(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = splitBetweenTwoPositions(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues1));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  // Extract Rules related helper methods.
  
  public void qsExtractFirstNChars(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = extractFirstNCharacters(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsExtractLastNChars(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = extractLastNCharacters(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsExtractTypeMismatched(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = typeMismatched(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsExtractQueryString(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = extractQueryString(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsExtractNumber(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = extractNumber(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsExtractBetweenTwoDelimiters(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1, String ruleInputValues2, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = extractBetweenTwoDelimiters(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues2));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsExtractTextOrPattern(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1, String ruleInputValues2, String ruleInputValues3, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = extractTextOrPattern(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues3));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsExtractCharsInBetween(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1, String ruleInputNewcolumns) {
    cleanseRuleInvocation.append("df = charactersBetweenTwoPositions(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues1));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputNewcolumns);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  // Format Rules related helper methods.
  
  public void qsAddSuffix(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues) {
    cleanseRuleInvocation.append("df = addSuffix(");
    commonMethod(cleanseRuleInvocation, escapeSingleDblQuotes(ruleInputValues), ruleImpactedCols2);
  }

  public void qsAddPrefix(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues) {
    cleanseRuleInvocation.append("df = addPrefix(");
    commonMethod(cleanseRuleInvocation, escapeSingleDblQuotes(ruleInputValues), ruleImpactedCols2);
  }

  public void qsFormatProper(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = formatProper(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }

  public void qsTrimQuotes(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = trimQuotes(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsTrimWhitespaces(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = trimWhitespaces(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsRemoveSpecialChars(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = removeSpecialCharacters(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }

  public void qsPaddingLeadingChar(
      StringBuilder cleanseRuleInvocation,
      String ruleImpactedCols2,
      String prefixValue,
      String repeat) {
    int repeatCount = 5;
    try {
      if(repeat != null) {
        repeatCount = Integer.parseInt(repeat);
      }
    }catch(Exception e) {
      // Do nothing...
    }
    
    cleanseRuleInvocation.append("df = padWithLeadingCharacters(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(repeatCount);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(prefixValue);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }

  public void qsRemoveAscents(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = removeAscents(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }

  public void qsTrimWhitespace(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = removeTrim(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsFormatLower(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = formatLower(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }

  public void qsFormatUpper(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = formatUpper(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  // Filter Rules related helper methods.
  
  public void qsTopRows(StringBuilder cleanseRuleInvocation, String ruleInputValues, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = topRows(");
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues5);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsTopRowsByColumn(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues1, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = topRowsByColumn(");
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues1));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues5);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsTopRowsAtRegularInterval(StringBuilder cleanseRuleInvocation, String ruleInputValues, String ruleInputValues1, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = topRowsAtRegularIntervalByDefault(");
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues1));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues5);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsTopRowsAtRegularIntervalByColumn(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = topRowsAtRegularInterval(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues1));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues5);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsRangeRows(StringBuilder cleanseRuleInvocation, String ruleInputValues1, String ruleInputValues2, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = rangeRows(");
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues1));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues2));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues5);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsRangeRowsByColumn(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues1, String ruleInputValues2, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = rangeRowsByColumn(");
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues1));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(Integer.parseInt(ruleInputValues2));
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues5);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsIsMissing(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = isMissing(");
    commonMethod(cleanseRuleInvocation, ruleInputValues5, ruleImpactedCols2);
  }
  
  public void qsIsExactly(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = isExactly(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleInputValues5, escapeSingleDblQuotes(ruleInputValues));
  }

  public void qsNotEqualTo(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = notEqualTo(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleInputValues5, ruleInputValues);
  }
  
  public void qsColumnValueIsOneOf(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = columnValueIsOneOf(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleInputValues5, ruleInputValues);
  }
  
  public void qsLessThanOrEqualTo(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = lessThanOrEqualTo(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleInputValues5, ruleInputValues);
  }
  
  public void qsGreaterThanOrEqualTo(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = greaterThanOrEqualTo(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleInputValues5, ruleInputValues);
  }
  
  public void qsIsBetween(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = isBetween(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues5);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsIsContains(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = contains(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleInputValues5, ruleInputValues);
  }
  
  public void qsStartsWith(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = startsWith(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleInputValues5, ruleInputValues);
  }
  
  public void qsEndsWith(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues5) {
    cleanseRuleInvocation.append("df = endsWith(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleInputValues5, ruleInputValues);
  }
  
  // Manage Columns related helper methods..
  
  public void qsManageColumnsCreate(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1) {
    cleanseRuleInvocation.append("df = createColumn(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues.toLowerCase());
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsManageColumnsDrop(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = dropColumn(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsManageColumnsRename(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues) {
    cleanseRuleInvocation.append("df = renameColumn(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues.toLowerCase());
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsManageColumnsClone(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = cloneColumn(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  // Replace rules related helper methods.
  
  public void qsReplaceTextOrPattern(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1) {
    cleanseRuleInvocation.append("df = replaceTextOrPattern(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append("re.escape(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")");
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append("re.escape(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")");
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsReplaceCells(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1) {
    cleanseRuleInvocation.append("df = replaceCells(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  // Missing rules related helper methods.
  
  public void qsMissingWithCustomValues(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues3) {
    cleanseRuleInvocation.append("df = missingWithCustomValues(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues3);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsMissingWithLastValue(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = missingWithLatestValues(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsMissingWithNullValue(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = missingWithNullValues(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsMissingWithAverageValue(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = missingWithAverageValues(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsMissingWithSumValue(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = missingWithSumValues(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsMissingWithModValue(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = missingWithModValues(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  // Count Rules related helper methods.
  
  public void qsCountMatchText(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues2) {
    cleanseRuleInvocation.append("df = countByCharacter(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }
  
  public void qsCountMatchDelimeter(StringBuilder cleanseRuleInvocation, String ruleImpactedCols2, String ruleInputValues, String ruleInputValues1, String ruleInputValues2) {
    cleanseRuleInvocation.append("df = countByDelimiter(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues1);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(")\n");
  }

  // Other miscellaneous rules related helper methods.
  
  public void qsGroupByAvg(
      StringBuilder cleanseRuleInvocation, String ruleInputValues, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = groupByavg(");
    commonMethod(cleanseRuleInvocation, ruleInputValues, ruleImpactedCols2);
  }

  public void qsGroupByMin(
      StringBuilder cleanseRuleInvocation, String ruleInputValues, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = groupBymin(");
    commonMethod(cleanseRuleInvocation, ruleInputValues, ruleImpactedCols2);
  }

  public void qsGroupByMax(
      StringBuilder cleanseRuleInvocation, String ruleInputValues, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = groupBymax(");
    commonMethod(cleanseRuleInvocation, ruleInputValues, ruleImpactedCols2);
  }

  public void qsGroupBySum(
      StringBuilder cleanseRuleInvocation, String ruleInputValues, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = groupBysum(");
    commonMethod(cleanseRuleInvocation, ruleInputValues, ruleImpactedCols2);
  }
  
  public void qsCountMatch(
      StringBuilder cleanseRuleInvocation,
      String ruleInputValues,
      String ruleOutputValues,
      String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = countMatch(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleImpactedCols2);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleInputValues, ruleOutputValues);
  }

  public void qsFillNull(
      StringBuilder cleanseRuleInvocation, String ruleOutputValues, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = fillNull(");
    commonMethod(cleanseRuleInvocation, ruleOutputValues, ruleImpactedCols2);
  }

  public void qsSplitTwoRule(
      StringBuilder cleanseRuleInvocation, String ruleDelimiter, String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = split2Rule("); // TODO
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append("column_1"); // column_1
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append("column_2"); // column_2
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleDelimiter, ruleImpactedCols2);
  }

  public void qsReplaceCell(
      StringBuilder cleanseRuleInvocation,
      String ruleInputValues,
      String ruleOutputValues,
      String ruleImpactedCols2) {
    cleanseRuleInvocation.append("df = findReplace(");
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(ruleInputValues);
    cleanseRuleInvocation.append(singleQuote);
    cleanseRuleInvocation.append(comma);
    commonMethod(cleanseRuleInvocation, ruleImpactedCols2, ruleOutputValues);
  }
  
  private String escapeSingleDblQuotes(String input) {
    if(input == null || input.isEmpty()) return "";
    
    String output = input;
    output = output.replaceAll("'", "\\\\'");
    output = output.replaceAll("\"", "\\\\\"");
    
    return output;
  }
  
}
