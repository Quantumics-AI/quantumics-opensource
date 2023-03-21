import { Rule, Column } from '../models/rule';
import { BaseRule } from './base-rule';

export class CountMatchesRule extends BaseRule {
  newColumnName;
  selectedColumns;
  patternToCount;
  isPreview: boolean;
  constructor(
    private rule: Rule,
    rows: Array<any>,
    private columns: Array<Column>
  ) {
    super(rows);

    this.selectedColumns = rule.ruleImpactedCols;
    this.newColumnName = rule.ruleInputValues2;

    this.patternToCount = rule.ruleInputValues;
  }


  public preview(): any {
    this.isPreview = true;
    this.addColumnTemp(this.columns, this.selectedColumns, this.newColumnName, this.rule.cleansingParamId ?? -1);
    this.performAction();
    return {
      data: this.rows,
      columns: this.columns
    };
  }

  public apply(): any {
    this.isPreview = false;
    this.addColumnTemp(this.columns, this.selectedColumns, this.newColumnName, this.rule.cleansingParamId ?? -1);
    this.performAction();
    this.columns.forEach(c => c.preview = '');
    return {
      data: this.rows,
      columns: this.columns
    };
  }

  performAction(): void {
    this.replaceAll(this.newColumnName, (row) => this.getNewValue(row));
  }

  private getNewValue(row: any): string {
    let count = 0;
    const value = row[this.selectedColumns];
    if (this.rule.ruleInputLogic1 === 'Text') {
      count = this.countOccurrence(value, this.patternToCount);
    } else if (this.rule.ruleInputLogic1 === 'Delimiter') {
      const startingDelimiter = this.rule.ruleInputValues ?? '';
      const endingDelimiter = this.rule.ruleInputValues1 ?? '';
      const pattern = `${startingDelimiter}(.*?)${endingDelimiter}`;

      const regularExpression = new RegExp(pattern, 'gi');
      const matchess = value.matchAll(regularExpression);
      count = Array.from(matchess, x => x[1]).length;
    }

    return count?.toString();
  }

  private countOccurrence(data: string, letter: string) {
    let letterCount = 0;
    if (data) {
      if (letter.length <= 1) {
        for (let position = 0; position < data.length; position++) {
          if (data.charAt(position) === letter) {
            letterCount = letterCount + 1;
          }
        }
        return letterCount;
      } else {
        return data.split(letter).length - 1;
      }
    }
  }

  private addColumnTemp(columns: Array<Column>, columnName: string, newColumnName: string, parentRuleId: number = -1): void {
    const sourceColumn = columns.find(c => c.column_name === columnName);
    const position = columns.findIndex(c => c.column_name === columnName);

    const previewColumn: Column = {
      column_name: newColumnName,
      data_type: sourceColumn?.data_type,
      preview: 'new',
      parentRuleId
    };

    columns.splice(position + 1, 0, previewColumn);
  }
}
