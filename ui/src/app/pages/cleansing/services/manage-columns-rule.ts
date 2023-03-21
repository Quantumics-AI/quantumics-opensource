import { ManageColumnRuleTypes } from '../constants/manage-column-rule-types';
import { Rule, Column } from '../models/rule';
import { BaseRule } from './base-rule';

export class ManageColumnsRule extends BaseRule {
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
        if (this.rule.ruleInputLogic1 === ManageColumnRuleTypes.Create) {
            if (!this.rule.ruleInputValues) {
                return;
            }

            // add preview column
            this.addColumn(this.columns, impactedColumn, this.rule.ruleInputValues, this.rule.cleansingParamId ?? -1);

            // copy data
            for (const row of this.rows) {
                row[this.rule.ruleInputValues] = this.rule.ruleInputValues1;
            }
        } else if (this.rule.ruleInputLogic1 === ManageColumnRuleTypes.Drop) {
            const idx: number = this.columns.findIndex(c => c.column_name === impactedColumn);
            if (this.isPreview) {
                this.columns[idx].preview = 'old';
            } else {
                this.columns.splice(idx, 1);
            }
        } else if (this.rule.ruleInputLogic1 === ManageColumnRuleTypes.Clone) {
            const newColumn1 = `${impactedColumn}_clone`;

            const startIndex = this.existingPreviewColumns(newColumn1);

            const newColumn = `${newColumn1}${startIndex}`;

            // add preview column
            this.addColumn(this.columns, impactedColumn, newColumn, this.rule.cleansingParamId ?? -1);

            // copy data
            for (const row of this.rows) {
                row[newColumn] = row[impactedColumn];
            }
        } else if (this.rule.ruleInputLogic1 === ManageColumnRuleTypes.Rename) {
            const idx: number = this.columns.findIndex(c => c.column_name === impactedColumn);
            this.renameColumn(this.columns, impactedColumn, this.rule.ruleInputValues, this.rule.cleansingParamId ?? -1);
            // copy data
            for (const row of this.rows) {
                row[this.rule.ruleInputValues] = row[impactedColumn];
            }
            if (this.isPreview) {
                this.columns[idx].preview = 'old';
            } else {
                this.columns.splice(idx, 1);
            }
        }
    }

    private getImpactedColumns(): Array<string> {
        return this.rule.ruleImpactedCols?.split(',') ?? [];
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
}
