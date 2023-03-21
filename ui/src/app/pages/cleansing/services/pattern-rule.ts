import { Rule, Column } from '../models/rule';
import { BaseRule } from './base-rule';
export class PatternRule extends BaseRule {

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
            const newValue = this.getNewValue();
            // const format = /[ `!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~]/;
            // const replace = format.test(this.rule.ruleInputValues) ? `\\${this.rule.ruleInputValues}` : this.rule.ruleInputValues;
            const replace = this.rule.ruleInputValues;
            this.replace(column, replace, newValue);
        }
        return this.rows;
    }

    private getImpactedColumns(): Array<string> {
        return this.rule.ruleImpactedCols?.split(',') ?? [];
    }

    private getNewValue(): string {
        return this.rule.ruleInputValues1;
    }
}
