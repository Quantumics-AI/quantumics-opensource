import { SplitRuleTypes } from '../constants/split-rule-types';
import { Column, Rule } from '../models/rule';
import { BaseRule } from './base-rule';

export class SplitRule extends BaseRule {
  private isPreview: boolean;
  constructor(
    private rule: Rule,
    rows: Array<any>,
    private columns: Array<Column>
  ) {
    super(rows);
  }

  public preview(): any {
    this.isPreview = true;
    this.addColumns();
    const impactedColumns = this.columns.filter(c => c.preview === 'old').map(c => c.column_name);
    this.performAction(impactedColumns);

    return {
      data: this.rows,
      columns: this.columns
    };
  }

  public apply(): any {
    this.isPreview = false;
    this.addColumns();
    const impactedColumns = this.columns.filter(c => c.preview === 'old').map(c => c.column_name);
    this.performAction(impactedColumns);

    // this.columns = this.columns.filter(c => c.preview !== 'old');

    this.columns.forEach(c => c.preview = '');

    return {
      data: this.rows,
      columns: this.columns
    };
  }

  private addColumns(): void {
    const impactedColumns: Array<string> = this.getImpactedColumns();
    for (const column of impactedColumns) {
      const startIndex = this.existingPreviewColumns(column);
      const previewColumns = this.previewColumns + startIndex - 1;

      for (let i = previewColumns; i >= startIndex; i--) {
        // add preview column
        this.addPreviewColumn(this.columns, column, `${this.columnSuffix}${i}`, this.rule.cleansingParamId ?? -1);

        // copy data
        const previewColumn = `${column}${this.columnSuffix}${i}`;
        for (const row of this.rows) {
          row[previewColumn] = row[column];
        }
      }
    }
  }

  private performAction(impactedColumns: Array<string>): any {
    for (const column of impactedColumns) {
      const startIndex = this.existingPreviewColumns(column);
      const previewColumns = this.previewColumns + startIndex - 1;

      for (const row of this.rows) {
        const sourceValue = row[column] ?? '';
        const values = this.splitValue(sourceValue);

        let i = 0;
        for (let index = startIndex; index <= previewColumns; index++) {
          row[`${column}${this.columnSuffix}${index}`] = values[i];
          i++;
        }
      }
    }

    return this.rows;
  }

  private getImpactedColumns(): Array<string> {
    return this.rule.ruleImpactedCols?.split(',') ?? [];
  }


  private get previewColumns(): number {
    if (this.rule.ruleInputLogic1 === SplitRuleTypes.ByPositions) {
      return this.rule.ruleInputValues.split(',').length + 1;
    }

    // return default value
    return 2;
  }

  private splitValue(value: string): Array<string> {
    if (this.rule.ruleInputLogic1 === SplitRuleTypes.ByDelimiter) {
      return this.byDelimiter(value);
    } else if (this.rule.ruleInputLogic1 === SplitRuleTypes.BetweenDelimiter) {
      return this.betweenDelimiter(value);
    } else if (this.rule.ruleInputLogic1 === SplitRuleTypes.ByPositions) {
      return this.byPositions(value);
    } else if (this.rule.ruleInputLogic1 === SplitRuleTypes.BetweenTwoPositions) {
      return this.betweenPosition(value);
    } else {
      return this.byDelimiter(value);
    }
  }

  private betweenDelimiter(sourceValue: string): Array<string> {

    const d1 = this.rule.ruleInputValues;
    const d2 = this.rule.ruleInputValues1;

    let value1 = '';
    let value2 = '';

    const index1 = sourceValue.indexOf(d1);
    const index2 = sourceValue.indexOf(d2, index1 + 1);

    if (index1 === -1 || index2 === -1) {
      value1 = sourceValue;
      value2 = 'null';
    } else if (index1 + d1.length >= index2 + d2.length) {
      value1 = sourceValue;
      value2 = 'null';
    } else {
      value1 = sourceValue.substr(0, sourceValue.indexOf(d1));
      const remaining = sourceValue.substr(sourceValue.indexOf(d1) + d1.length);
      value2 = remaining.substr(remaining.indexOf(d2) + d2.length);
    }

    return [value1, value2];
  }

  private byDelimiter(sourceValue: string): Array<string> {
    const delimiter = this.rule.ruleInputValues;
    const value1 = sourceValue.substr(0, sourceValue.indexOf(delimiter));
    const value2 = sourceValue.substr(sourceValue.indexOf(delimiter) + delimiter?.length);
    return [value1, value2];
  }

  private byPositions(sourceValue: string): Array<string> {
    const values = [];
    const positionsString = this.rule.ruleInputValues;
    const tempPositions = positionsString.split(',');
    const positions = tempPositions.map(p => +p);
    let previousPosition = 0;
    for (const p of positions) {
      const newValue = sourceValue.substr(previousPosition, p - previousPosition);
      previousPosition = p;
      values.push(newValue);
    }

    values.push(sourceValue.substr(previousPosition));

    return values;
  }

  private betweenPosition(sourceValue: string): Array<string> {
    const p1 = +this.rule.ruleInputValues;
    const p2 = +this.rule.ruleInputValues1;

    let value1 = '';
    let value2 = '';

    if (p1 > p2) {
      value1 = sourceValue;
      value2 = 'null';
    } else {
      value1 = sourceValue.substr(0, p1);
      value2 = sourceValue.substr(p2);
    }

    return [value1, value2];
  }


  private existingPreviewColumns(column: string): number {
    const regEx = new RegExp(`^${column}\\d+$`);
    const endingNumbersRegEx = /\d+$/;

    const previewColumns = this.columns.filter(c => regEx.test(c.column_name) && c.preview !== 'new').map(c => {
      const matches = c.column_name.match(endingNumbersRegEx);
      if (matches) {
        return parseInt(matches[0], 10);
      } else {
        return 1;
      }
    });

    if (previewColumns.length) {
      return Math.max.apply(Math, previewColumns) + 1;
    } else {
      return 1;
    }
  }

  private get columnSuffix(): string {
    return this.isPreview ? '_new' : '';
  }
}
