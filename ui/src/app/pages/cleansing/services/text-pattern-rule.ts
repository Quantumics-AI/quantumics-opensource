import { Rule, Column } from '../models/rule';
import { BaseRule } from './base-rule';

export class TextPatternRule extends BaseRule {
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


    private performAction(impactedColumns: Array<string>): any {
        for (const column of impactedColumns) {
            this.replaceAll(column, (row) => this.getNewValue(column, row));
        }
        return this.rows;
    }

    private getImpactedColumns(): Array<string> {
        return this.rule.ruleImpactedCols?.split(',') ?? [];
    }

    private getNewValue(column: string, row: any): string {
        const value = row[column] ? row[column] : '';
        const replaceStr = this.escapeRegExp(this.rule.ruleInputValues);
        // make it case insensetive as per #1543
        const regEx = new RegExp(replaceStr, 'g');
        const replacedValue = this.rule.ruleInputValues1 ? value.replace(regEx, this.rule.ruleInputValues1) : value.replace(regEx, '');
        return replacedValue;
    }


    private escapeRegExp(str: string): string {
        return str?.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }
}
