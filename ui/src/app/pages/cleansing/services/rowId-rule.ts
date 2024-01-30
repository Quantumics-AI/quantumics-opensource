import { Column, Rule } from '../models/rule';
import { BaseRule } from './base-rule';

export class RowIdRule extends BaseRule {
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

    private performAction(): void {
        const impactedColumn: string = this.getImpactedColumns()[0];
        let position=0;
        if (this.rule.ruleInputValues1 === 'First Column') {
          position=0;
        }else{
          position=this.columns?.length;
        }
        this.addColumn(this.columns, impactedColumn,this.rule.ruleInputValues,-1, position );
        for (let index = 0; index < this.rows.length; index++) {
            const row = this.rows[index];
            const result = Number(this.rule.ruleInputValues2) + index;
            row[this.rule.ruleInputValues] = result;
        }
    }

    private getImpactedColumns(): Array<string> {
        return this.rule.ruleImpactedCols?.split(',') ?? [];
    }
   
}
