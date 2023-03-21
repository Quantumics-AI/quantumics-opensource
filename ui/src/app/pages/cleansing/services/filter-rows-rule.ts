import { FilterRuleActionTypes, FilterRuleTypes } from '../constants/filter-rule-types';
import { Column, Rule } from '../models/rule';
import { BaseRule } from './base-rule';
import { sortBy } from 'lodash';

export class FilterRowsRule extends BaseRule {
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
        this.apply1();
        return {
            data: this.rows,
            columns: this.columns
        };
    }

    public apply(): any {
        this.apply1();
        return {
            data: this.rows,
            columns: this.columns
        };
    }

    private get selectedColumns(): Array<string> {
        const columns = this.rule.ruleImpactedCols?.split(',') ?? [];
        return columns.filter(c => this.columns.some(cc => cc.column_name === c));
    }

    private get ruleType(): string {
        return this.rule.ruleInputLogic1;
    }

    private get rulesInput(): string {
        return this.rule.ruleInputValues;
    }

    private get rulesInput1(): string {
        return this.rule.ruleInputValues1;
    }

    private get action(): string {
        return this.rule.ruleInputValues5;
    }

    private get sortColumns(): Array<string> {
        return this.rule.ruleImpactedCols?.split(',');
    }

    private apply1(): any {
        switch (this.ruleType) {
            case FilterRuleTypes.TopRows:
                this.rows = sortBy(this.rows, this.sortColumns);
                this.removeTopRows();
                break;
            case FilterRuleTypes.Range:
                this.rows = sortBy(this.rows, this.sortColumns);
                this.removeRange();
                break;
            case FilterRuleTypes.RegularInterval:
                this.rows = sortBy(this.rows, this.sortColumns);
                this.removeRegularInterval();
                break;
            case FilterRuleTypes.IsMissing:
                this.removeMissingRows();
                break;
            case FilterRuleTypes.EqualTo:
                this.removeEqualTo();
                break;
            case FilterRuleTypes.NotEqualTo:
                this.removeNotEqualTo();
                break;
            case FilterRuleTypes.GreaterThanOrEqualTo:
                this.removeGreaterThanOrEqualTo();
                break;
            case FilterRuleTypes.LessThanOrEqualTo:
                this.removeLessThanOrEqualTo();
                break;
            case FilterRuleTypes.Contains:
                this.removeContains();
                break;
            case FilterRuleTypes.StartsWith:
                this.removeStartsWith();
                break;
            case FilterRuleTypes.EndsWith:
                this.removeEndsWith();
                break;
            case FilterRuleTypes.IsBetween:
                this.removeIsBetween();
                break;
            case FilterRuleTypes.IsOneOf:
                this.removeIsOneOf();
                break;
            case FilterRuleTypes.TypeMismatched:
                this.removeMismatched();
                break;
            default:
                break;
        }

        return this.rows;
    }

    private removeMissingRows(): void {
        for (const col of this.selectedColumns) {
            const matchingRows = this.rows.filter(r => !r[col]);
            this.performActionN(matchingRows);
        }
    }

    private removeEqualTo(): void {
        for (const col of this.selectedColumns) {
            // tslint:disable-next-line: triple-equals
            const matchingRows = this.rows.filter(r => r[col] == this.rulesInput);
            this.performActionN(matchingRows);
        }
    }

    private removeNotEqualTo(): void {
        for (const col of this.selectedColumns) {
            // tslint:disable-next-line: triple-equals
            const matchingRows = this.rows.filter(r => r[col] != this.rulesInput);
            this.performActionN(matchingRows);
        }
    }

    private removeGreaterThanOrEqualTo(): void {
        for (const col of this.selectedColumns) {
            const matchingRows = this.rows.filter(r => r[col]?.trim()?.length !== 0 && r[col] >= +this.rulesInput);
            this.performActionN(matchingRows);
        }
    }

    private removeLessThanOrEqualTo(): void {
        for (const col of this.selectedColumns) {
            const matchingRows = this.rows.filter(r => r[col]?.trim()?.length !== 0 && r[col] <= +this.rulesInput);
            this.performActionN(matchingRows);
        }
    }

    private removeContains(): void {
        for (const col of this.selectedColumns) {
            const matchingRows = this.rows.filter(r => r[col]?.includes(this.rulesInput));
            this.performActionN(matchingRows);
        }
    }

    private removeStartsWith(): void {
        for (const col of this.selectedColumns) {
            const matchingRows = this.rows.filter(r => r[col]?.startsWith(this.rulesInput));
            this.performActionN(matchingRows);
        }
    }

    private removeEndsWith(): void {
        for (const col of this.selectedColumns) {
            const matchingRows = this.rows.filter(r => r[col]?.endsWith(this.rulesInput));
            this.performActionN(matchingRows);
        }
    }

    private removeIsBetween(): void {
        for (const col of this.selectedColumns) {
            const startValue = +this.rulesInput;
            const endValue = +this.rulesInput1;
            const matchingRows = this.rows.filter(r => r[col]?.trim()?.length !== 0 && +r[col] >= startValue && +r[col] <= endValue);
            this.performActionN(matchingRows);
        }
    }

    private removeIsOneOf(): void {
        for (const col of this.selectedColumns) {
            const inputValues = this.rulesInput.split(',');
            const matchingRows = this.rows.filter(r => inputValues.includes(r[col]));
            this.performActionN(matchingRows);
        }
    }

    private removeMismatched(): void {
        for (const col of this.selectedColumns) {
            const columnType = this.columns.find(c => c.column_name === col)?.data_type;
            const matchingRows = this.rows.filter((r => typeof r[col] !== columnType));
            this.performActionN(matchingRows);
        }
    }

    private removeTopRows(): void {
        const matchingRows = this.rows.slice(0, +this.rulesInput);
        this.performActionN(matchingRows);
    }

    private removeRange(): void {
        let startIndex = +this.rulesInput;
        if (startIndex > 0) {
            // -1 for 0 based index
            startIndex = startIndex - 1;
        }

        const endIndex = +this.rulesInput1;
        const matchingRows = this.rows.slice(startIndex, endIndex);
        this.performActionN(matchingRows);
    }

    private removeRegularInterval(): void {
        const interval = +this.rulesInput;
        let startRow = +this.rulesInput1;
        if (startRow > 0) {
            // -1 for 0 based index
            startRow = startRow - 1;
        }
        const matchingRows = [];
        for (let i = startRow; i < this.rows.length; i = i + interval) {
            matchingRows.push(this.rows[i]);
        }


        this.performActionN(matchingRows);
    }

    private performActionN(matchingRows: Array<any>): void {
        if (this.action === FilterRuleActionTypes.Delete) {
            if (this.isPreview) {
                matchingRows.forEach(r => (r.preview__row = true));
            } else {
                this.rows = this.rows.filter(r => !matchingRows.includes(r));
            }
        } else if (this.action === FilterRuleActionTypes.Keep) {
            if (this.isPreview) {
                this.rows.filter(r => !matchingRows.includes(r)).forEach(r => r.preview__row = true);
            } else {
                this.rows = matchingRows;
            }
        }
    }
}
