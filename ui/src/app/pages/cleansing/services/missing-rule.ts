import { BaseRule } from './base-rule';
import { Rule, Column } from '../models/rule';
import { ReplaceMissingRuleTypes } from '../constants/replace-missing-rule-types';

import { countBy, head, entries, maxBy, last, flow, partialRight } from 'lodash';

export class MissingRule extends BaseRule {
    constructor(
        private rule: Rule,
        rows: Array<any>,
        private columns: Array<Column>
    ) {
        super(rows);
    }

    public preview(): any {
        let impactedColumns: Array<string> = this.getImpactedColumns();
        for (const column of impactedColumns) {
            // add preview column
            this.addPreviewColumn(this.columns, column, '_new');

            // copy data
            const previewColumn = `${column}_new`;
            for (const row of this.rows) {
                row[previewColumn] = row[column];
            }
        }

        impactedColumns = this.columns.filter(c => c.preview === 'new').map(c => c.column_name);
        this.performAction(impactedColumns);

        return {
            data: this.rows,
            columns: this.columns
        };
    }

    public apply(): any {
        const impactedColumns: Array<string> = this.getImpactedColumns();
        this.performAction(impactedColumns);

        return {
            data: this.rows,
            columns: this.columns
        };
    }

    private getImpactedColumns(): Array<string> {
        return this.rule.ruleImpactedCols?.split(',') ?? [];
    }

    private performAction(impactedColumns: Array<string>): void {
        for (const column of impactedColumns) {
            let newValue: string | number = '';
            switch (this.rule.ruleInputLogic1) {
                case ReplaceMissingRuleTypes.Average:
                    newValue = this.getAverage(column);
                    this.replace(column, '', newValue);
                    break;
                case ReplaceMissingRuleTypes.CustomValue:
                    newValue = this.rule.ruleInputValues3;
                    this.replace(column, '', newValue);
                    break;
                case ReplaceMissingRuleTypes.LastValidValue:
                    for (const [i, row] of this.rows.entries()) {
                        const currentValue = row[column] !== undefined ? row[column] : '';
                        if (!currentValue.trim()) {
                            if (i === 0) {
                                newValue = '';
                            } else {
                                newValue = this.rows[i - 1][column];
                            }
                            row[column] = newValue;
                        }
                    }
                    break;
                case ReplaceMissingRuleTypes.Mod:
                    const tempArray = this.rows.map((r) => r[column]).filter(t => t?.trim() !== '');
                    const mode: any = flow(
                        countBy,
                        entries,
                        partialRight(maxBy, last),
                        head
                    )(tempArray);

                    this.replace(column, '', mode);
                    break;
                case ReplaceMissingRuleTypes.Sum:
                    newValue = this.getSum(column);
                    this.replace(column, '', newValue);
                    break;
                case ReplaceMissingRuleTypes.Null:
                    newValue = 'Null';
                    this.replace(column, '', newValue);
                    break;
                default:
                    break;
            }
        }
    }

    private getAverage(column: string): string {
        const sum = this.getSum(column);
        if (sum === '0') {
            return '';
        } else {
            const c = this.rows.filter(r => !isNaN(r[column]?.trim())).filter(r => r[column]?.trim() !== '').length;
            return (+sum / c).toString();
        }
    }

    private getSum(column: string): string {
        const sum = this.rows.reduce((acc, item) => {
            let currentValue;
            if (isNaN(item[column])) {
                currentValue = 0;
            } else {
                currentValue = +item[column];
            }
            return acc + currentValue;
        }, 0);

        return sum.toString();
    }
}
