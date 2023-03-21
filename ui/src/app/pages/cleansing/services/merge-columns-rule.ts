import { Rule, Column } from '../models/rule';
import { BaseRule } from './base-rule';

export class MergeColumnsRule extends BaseRule {
    private isPreview: boolean;
    private impactedColumns: Array<string>;
    constructor(
        private rule: Rule,
        rows: Array<any>,
        private columns: Array<Column>
    ) {
        super(rows);
        this.impactedColumns = this.getImpactedColumns();
    }

    public preview(): any {

        this.performAction();

        return {
            data: this.rows,
            columns: this.columns
        };
    }

    public apply(): any {
        this.isPreview = false;
        this.performAction();

        this.columns.forEach(c => c.preview = '');

        return {
            data: this.rows,
            columns: this.columns
        };
    }


    private performAction() {
        const len = this.impactedColumns.length;
        const { [len - 1]: lastSelectedColumn } = this.impactedColumns;

        this.isPreview = true;
        const newColumn = this.rule.ruleInputValues;

        this.addColumn(this.columns, lastSelectedColumn, newColumn, this.rule.cleansingParamId ?? -1);

        this.replaceAll(newColumn, (rows) => this.getNewValue(rows));
    }

    private getImpactedColumns(): Array<string> {
        return this.rule.ruleImpactedCols?.split(',') ?? [];
    }

    private getNewValue(row: any): string {
        let val = '';
        this.impactedColumns.forEach((col, index) => {
            const temp = row[col] ? row[col] : '';
            if (temp !== undefined) {
                val += `${temp}${this.rule.ruleDelimiter}`;
            }

            // if (index !== this.impactedColumns.length - 1) {
            //     val += ' ';
            // }
        });

        val = val.slice(0, val.length - this.rule.ruleDelimiter.length);
        return val;
    }
}
