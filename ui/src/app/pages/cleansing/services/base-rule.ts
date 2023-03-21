import { Column } from '../models/rule';

export class BaseRule {
    constructor(public rows: any[]) {

    }

    replace(column: string, oldValue: string, newValue: string): void {
        for (const row of this.rows) {
            const currentValue = row[column] !== undefined ? row[column] : '';
            if (String(currentValue?.trim())?.toLowerCase() === String(oldValue?.trim())?.toLowerCase()) {
                row[column] = newValue;
            }
        }
    }

    replaceAll(column: string, newValueCallback: any): void {
        for (const row of this.rows) {
            row[column] = newValueCallback(row);
        }
    }

    addPreviewColumn(columns: Array<Column>, columnName: string, suffix: string, parentRuleId: number = -1): void {
        const sourceColumn = columns.find(c => c.column_name === columnName);
        const position = columns.findIndex(c => c.column_name === columnName);

        sourceColumn.preview = 'old';
        const previewColumn: Column = {
            column_name: `${sourceColumn.column_name}${suffix}`,
            data_type: sourceColumn.data_type,
            preview: 'new',
            parentRuleId
        };

        columns.splice(position + 1, 0, previewColumn);
    }

    addColumn(columns: Array<Column>, columnName: string, newColumnName: string, parentRuleId: number = -1): void {
        const sourceColumn = columns.find(c => c.column_name === columnName);
        const position = columns.findIndex(c => c.column_name === columnName);

        // sourceColumn.preview = 'old';
        const previewColumn: Column = {
            column_name: newColumnName,
            data_type: sourceColumn?.data_type,
            preview: 'new',
            parentRuleId
        };

        columns.splice(position + 1, 0, previewColumn);
    }

    renameColumn(columns: Array<Column>, currentColumnName: string, newColumnName: string, parentRuleId: number = -1): void {
        const sourceColumn = columns.find(c => c.column_name === currentColumnName);
        const position = columns.findIndex(c => c.column_name === currentColumnName);

        sourceColumn.preview = 'old';
        const previewColumn: Column = {
            column_name: newColumnName,
            data_type: sourceColumn.data_type,
            preview: 'new',
            parentRuleId
        };

        columns.splice(position + 1, 0, previewColumn);
    }

}
