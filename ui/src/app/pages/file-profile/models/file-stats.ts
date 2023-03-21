export class DataQuality {
    overview: Overview;
    attributeValidity: Array<AttributeValidity>;
}

export class Overview {
    qataQualityOverview: DataQualityOverview;
}

export class DataQualityOverview {
    overallQuality: number;
    recordCount: number;
    dqChecks: number;
    attributes: number;
}

export class AttributeValidity {
    attribute: number;
    validity: number;
    records: number;
    valid: number;
    inValid: number;
    dqRules: number;
    ddId: string;
}

export class Frequency {
    text: string;
    value: string;
}

export class Column {
    column: string;
    dataType: string;
    selected: boolean;
}

export class AttributeCount {
    null: number;
    notNull: number;
    duplicate: number;
    distinct: number;
}

export class StatisticsParams {
    mean: number;
    median: number;
    variance: number;
    min: number;
    max: number;
    sum: number;
    std: number;
}

export class ColumnsStats {
    attributeCounts: AttributeCount;
    statisticsParams: StatisticsParams;
    column: string;
}

export class Analysis {
    frequency: Frequency[];
    columns: Column[];
    columnsStats: ColumnsStats[];
    recordCount: number;
    columnCount: number;
    selectedColumn: string;
    selectedDataType?: string;
    fileSize: string;

    constructor() {
        this.frequency = [];
        this.columns = [];
        this.columnsStats = [];
    }
}
