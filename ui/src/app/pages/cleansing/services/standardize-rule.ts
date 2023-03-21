import { Rule, Column } from '../models/rule';
import { BaseRule } from './base-rule';

export class StandardizeRule extends BaseRule {
    constructor(
        private rule: Rule,
        rows: Array<any>,
        private columns: Array<Column>
    ) {
        super(rows);
    }

    apply(): any {
        const values = this.rule.ruleInputValues1.split('|');
        const selectedColumn = this.getImpactedColumns()[0];
        for (const value of values) {

            this.replace(selectedColumn, value, this.rule.ruleInputValues);
        }

        return {
            data: this.rows,
            columns: this.columns
        };
    }

    private getImpactedColumns(): Array<string> {
        return this.rule.ruleImpactedCols?.split(',') ?? [];
    }
}

