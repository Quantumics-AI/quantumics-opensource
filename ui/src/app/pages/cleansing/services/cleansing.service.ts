import { Injectable } from '@angular/core';
import { Column, Rule } from '../models/rule';
import { FormatRule } from './format-rule';
import { TextPatternRule } from './text-pattern-rule';
import { RuleTypes } from '../constants/rule-types';
import { PatternRule } from './pattern-rule';
import { MissingRule } from './missing-rule';
import { SplitRule } from './split-rule';
import { FilterRowsRule } from './filter-rows-rule';
import { ExtractColumnValuesRule } from './extract-column-values-rule';
import { ManageColumnsRule } from './manage-columns-rule';
import { MergeColumnsRule } from './merge-columns-rule';
import { FilterDuplicateRowsRule } from './filter-duplicate-rows-rule';
import { StandardizeRule } from './standardize-rule';
import { CountMatchesRule } from './count-matches-rule';

@Injectable({
  providedIn: 'root'
})
export class CleansingService {

  preview(rule: Rule, rows: any, columns: Array<Column>): any {
    const ruleObj = this.getObject(rule, rows, columns);
    return ruleObj.preview();
  }

  apply(rules: Array<Rule>, data: any, columns: Array<Column>): any {
    for (const rule of rules) {
      const ruleObj = this.getObject(rule, data, columns);
      const response = ruleObj.apply();
      data = response.data;
      columns = response.columns;
    }

    return {
      data,
      columns
    };
  }

  private getObject(rule: Rule, rows: any, columns: Array<Column>): any {
    const ruleType = rule.ruleInputLogic;
    let ruleObj: any;

    switch (ruleType) {
      case RuleTypes.Format:
        ruleObj = new FormatRule(rule, rows, columns);
        break;
      case RuleTypes.FindReplace:
        ruleObj = new PatternRule(rule, rows, columns);
        break;
      case RuleTypes.PatternReplace:
        ruleObj = new TextPatternRule(rule, rows, columns);
        break;
      case RuleTypes.ReplaceNull:
        ruleObj = new MissingRule(rule, rows, columns);
        break;
      case RuleTypes.SplitColumnValue:
        ruleObj = new SplitRule(rule, rows, columns);
        break;
      case RuleTypes.FilterRows:
      case RuleTypes.FilterRowsByColumn:
        ruleObj = new FilterRowsRule(rule, rows, columns);
        break;
      case RuleTypes.RemoveDuplicateRows:
        ruleObj = new FilterDuplicateRowsRule(rule, rows, columns);
        break;
      case RuleTypes.ExtractColumnValues:
        ruleObj = new ExtractColumnValuesRule(rule, rows, columns);
        break;
      case RuleTypes.ManageColumns:
        ruleObj = new ManageColumnsRule(rule, rows, columns);
        break;
      case RuleTypes.Merge:
        ruleObj = new MergeColumnsRule(rule, rows, columns);
        break;
      case RuleTypes.Standardize:
        ruleObj = new StandardizeRule(rule, rows, columns);
        break;
      case RuleTypes.CountMatch:
        ruleObj = new CountMatchesRule(rule, rows, columns);
        break;
    }

    return ruleObj;
  }
}
