import { FormatRuleTypes } from '../constants/format-rule-types';
import { Column, Rule } from '../models/rule';
import { BaseRule } from './base-rule';
import { removeAccents } from './remove-accents';

export class FormatRule extends BaseRule {
    private maxCellLength: number;

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

            if (this.rule.ruleInputLogic1 === FormatRuleTypes.PadWithLeadingCharacters) {
                const rowValueLengths = this.rows.map(r => r[column]).map(el => el?.length);
                this.maxCellLength = Math.max(...rowValueLengths);
            }

            this.replaceAll(column, (row) => this.getNewValue(column, row));
        }
        return this.rows;
    }

    private getNewValue(column: string, row: string): string {
        const value = row[column] !== undefined ? row[column] : '';
        switch (this.rule.ruleInputLogic1) {
            case FormatRuleTypes.Lower:
                return value.toLowerCase();
            case FormatRuleTypes.Upper:
                return value.toUpperCase();
            case FormatRuleTypes.Proper:
                return this.convertToProperCase(value);
            case FormatRuleTypes.TrimWhitespace:
                return value.trim();
            case FormatRuleTypes.TrimQuotes:
                return value.replace(/^["']+|["']+$/g, '');
            case FormatRuleTypes.RemoveWhitespaces:
                return value.replace(/ /g, '');
            case FormatRuleTypes.RemoveSpecialCharacters:
                // return value.replace(/[&%$#@]/g, '');
                return value.replace(/[^a-zA-Z0-9 ]/g, '');
            case FormatRuleTypes.RemoveAccents:
                // return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
                return removeAccents(value);
            case FormatRuleTypes.AddPrefix:
                return `${this.rule.ruleInputValues}${value}`;
            case FormatRuleTypes.AddSuffix:
                return `${value}${this.rule.ruleInputValues}`;
            case FormatRuleTypes.PadWithLeadingCharacters:
                return this.paddWithLeadingCharacters(value);
            default:
                return value;
        }
    }

    private convertToProperCase(value: string): string {
        return value.replace(/\b\w+('\w{1})?/g, match => match.charAt(0).toUpperCase() + match.substr(1).toLowerCase());
    }

    private paddWithLeadingCharacters(value: string): string {
        const padStr = this.rule.ruleInputValues;

        while (this.maxCellLength > value?.length) {
            value = `${padStr}${value}`;
        }

        const actualLength = value.length;
        const charactersToTrim = actualLength - this.maxCellLength;

        return value.substring(charactersToTrim);
    }

    private getImpactedColumns(): Array<string> {
        return this.rule.ruleImpactedCols?.split(',') ?? [];
    }
}
