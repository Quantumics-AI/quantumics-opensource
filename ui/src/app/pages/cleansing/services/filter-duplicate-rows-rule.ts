import { Column, Rule } from '../models/rule';
import { BaseRule } from './base-rule';

export class FilterDuplicateRowsRule extends BaseRule {
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
        this.performAction();
        return {
            data: this.rows,
            columns: this.columns
        };
    }

    private performAction(): void {
        const uniqueObjectsSet = new Set();
        const duplicteIndices = [];

        for (let i = 0; i < this.rows.length; i++) {
            const json = JSON.stringify(this.rows[i]);
            if (uniqueObjectsSet.has(json)) {
                duplicteIndices.push(i);
            }

            uniqueObjectsSet.add(json);
        }

        if (this.isPreview) {
            for (const i of duplicteIndices) {
                this.rows[i].preview__row = true;
            }
        } else {
            for (let i = duplicteIndices.length - 1; i >= 0; i--) {
                this.rows.splice(duplicteIndices[i], 1);
            }
        }
    }
}
