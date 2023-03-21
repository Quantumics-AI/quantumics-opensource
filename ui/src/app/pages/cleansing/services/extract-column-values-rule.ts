import { ExtractRuleTypes } from '../constants/extract-rule-types';
import { Column, Rule } from '../models/rule';
import { BaseRule } from './base-rule';

export class ExtractColumnValuesRule extends BaseRule {
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
        this.addColumns();
        const impactedColumns = this.columns.filter(c => c.preview === 'old').map(c => c.column_name);
        this.performAction(impactedColumns);

        return {
            data: this.rows,
            columns: this.columns
        };
    }

    public apply(): any {
        this.isPreview = false;
        this.addColumns();
        const impactedColumns = this.columns.filter(c => c.preview === 'old').map(c => c.column_name);
        this.performAction(impactedColumns);

        this.columns.forEach(c => c.preview = '');
        return {
            data: this.rows,
            columns: this.columns
        };
    }

    private addColumns(): void {
        const impactedColumns: Array<string> = this.getImpactedColumns();
        for (const column of impactedColumns) {
            const startIndex = this.existingPreviewColumns(column);
            const previewColumns = this.numberOfPreviewColumns + startIndex - 1;
            for (let i = previewColumns; i >= startIndex; i--) {
                // add preview column
                this.addPreviewColumn(this.columns, column, `${this.columnSuffix}${i}`, this.rule.cleansingParamId ?? -1);

                // copy data
                const previewColumn = `${column}${this.columnSuffix}${i}`;
                for (const row of this.rows) {
                    row[previewColumn] = row[column];
                }
            }
        }
    }

    private performAction(impactedColumns: Array<string>): any {
        for (const column of impactedColumns) {
            const startIndex = this.existingPreviewColumns(column);
            const previewColumns = this.numberOfPreviewColumns + startIndex - 1;

            for (const row of this.rows) {
                const sourceValue = row[column] ?? '';
                const values = this.extractValues(sourceValue) ?? [];

                let i = 0;
                for (let index = startIndex; index <= previewColumns; index++) {
                    row[`${column}${this.columnSuffix}${index}`] = values[i];
                    i++;
                }
            }
        }
        return this.rows;
    }

    private getImpactedColumns(): Array<string> {
        return this.rule.ruleImpactedCols?.split(',') ?? [];
    }

    private extractValues(value: any): Array<string> {
        let numberOfCharactersToExtract = 0;
        switch (this.rule.ruleInputLogic1) {
            case ExtractRuleTypes.Numbers:
                // return value.match(/[-+]?[0-9]*\.?[0-9]+/g);
                return value.match(/[0-9]*?[0-9]+/g);
            case ExtractRuleTypes.FirstCharacters:
                numberOfCharactersToExtract = +this.rule.ruleInputValues;
                return [value.substring(0, numberOfCharactersToExtract)];
            case ExtractRuleTypes.LastCharacters:
                numberOfCharactersToExtract = +this.rule.ruleInputValues;
                return [value.slice(-numberOfCharactersToExtract)];
            case ExtractRuleTypes.TypeMismatched:
                const type = this.rule.ruleInputValues;
                if (typeof value !== type) {
                    return [value];
                } else {
                    return [''];
                }
            case ExtractRuleTypes.CharactersBetweenPostions:
                const startIndex = +this.rule.ruleInputValues;
                const endIndex = +this.rule.ruleInputValues1;
                return [value.substring(startIndex, endIndex)];
            case ExtractRuleTypes.TextOrPattern:
                const textToExtract = this.rule.ruleInputValues ?? '';
                const extractAfter = this.rule.ruleInputValues1 ?? '';
                const endExtractionBefore = this.rule.ruleInputValues2 ?? '';

                const text = `${extractAfter}${textToExtract}${endExtractionBefore}`;
                const regEx = new RegExp(text, 'gi');
                const matches = value.match(regEx) ?? [];
                return matches.map(m => m ? textToExtract : '');
            case ExtractRuleTypes.QueryStrings:
                try {
                    const url = new URL(value);
                    const urlParams: any = new URLSearchParams(url.search);
                    const result = [];
                    for (const key of this.rule.ruleInputValues?.split(',')) {
                        result.push(urlParams.get(key));
                    }

                    return result;

                } catch {
                    return [];
                }
            case ExtractRuleTypes.BetweenDelimiters:
                const startingDelimiter = this.rule.ruleInputValues ?? '';
                const endingDelimiter = this.rule.ruleInputValues1 ?? '';

                let pattern;
                if (startingDelimiter && endingDelimiter) {
                    pattern = `${startingDelimiter}(.*?)${endingDelimiter}`;
                } else if (startingDelimiter) {
                    pattern = `${startingDelimiter}(.*)`;
                } else if (endingDelimiter) {
                    pattern = `(.*?)${endingDelimiter}`;
                }

                if (pattern) {
                    const regularExpression = new RegExp(pattern, 'g');
                    const matchess = value.matchAll(regularExpression);
                    return Array.from(matchess, x => x[1]);
                } else {
                    return [];
                }
        }
    }

    private get numberOfPreviewColumns(): number {
        switch (this.rule.ruleInputLogic1) {
            case ExtractRuleTypes.Numbers:
                return +this.rule.ruleInputValues;
            case ExtractRuleTypes.TextOrPattern:
                return +this.rule.ruleInputValues3;
            case ExtractRuleTypes.BetweenDelimiters:
                return +this.rule.ruleInputValues2;
            case ExtractRuleTypes.QueryStrings:
                return this.rule.ruleInputValues?.split(',').length;
            default:
                return 1;
        }
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

    private get columnSuffix(): string {
        return this.isPreview ? '_new' : '';
    }
}
