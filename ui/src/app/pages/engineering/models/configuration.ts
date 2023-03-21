export class Configuration {
    projectId: number;
    userId: number;
    engineeringId: number;
    selectedFiles: SelectedFile[];
    selectedJoins: SelectedJoin[];
    selectedUdfs: SelectedUdf[];
    selectedAggregates: SelectedAggregate[];

    constructor() {
        this.selectedFiles = [];
        this.selectedJoins = [];
        this.selectedUdfs = [];
        this.selectedAggregates = [];
    }
}

export class PreviewContent {
    rowData?: any;
    rowColumns?: any;
    eventProgress?: number;
    disabledPreview?: boolean;
    viewOnly?: boolean;
    isDisabledApply?: boolean;

    constructor() {
        this.eventProgress = 15;
        this.rowColumns = [];
        this.rowData = [];
        this.viewOnly = false;
        this.isDisabledApply = false;
    }
}


export class SelectedFile {
    // this are redundant fields but need to be present here for api to work
    projectId: number;
    engFlowId: number;

    autoConfigEventId: number;
    eventId: number;
    name: string;
    type: string;
    fileId: string;
    folderId: string;
    metaData: any;
    fileType: string;
    content?: PreviewContent;
}

export class SelectedJoin {
    // this are redundant fields but need to be present here for api to work
    projectId: number;
    engFlowId: number;

    autoConfigEventId: number;
    eventId: number;
    eventId1: number;
    eventId2: number;
    dfId: string;
    joinName: string;
    joinType: string;
    firstFileColumn: string;
    secondFileColumn: string;
    metaData: any;
    joinFolders: any;
    content?: PreviewContent;
}

export class SelectedUdf {
    // this are redundant fields but need to be present here for api to work
    projectId: number;
    engFlowId: number;

    autoConfigEventId: number;
    eventId: number;
    eventId1: number;
    eventId2: number;
    eventIds: {};
    dfId: string;
    joinName: string;
    joinType: string;
    firstFileColumn: string;
    secondFileColumn: string;
    metaData: any;
    joinFolders: any;
    udfFunction: any;
    arguments: any;
    fileType: string;
    fileId: number;
    variable : any;
    content?: PreviewContent;
    joinOperations?: string;
}

export class SelectedAggregate {
    projectId: number;
    engFlowId: number;
    eventId: number;
    autoConfigEventId: number;
    dataFrameEventId: number;
    groupByColumns: string[];
    groupByColumnsMetaData?: any;
    columns: AggregateColumn[];
    columnsMetaData?: any[];
    metaData: any;
    content?: PreviewContent;


    constructor() {
        this.groupByColumns = [];
        this.columns = [];
    }
}

export interface AggregateColumn {
    opName: string;
    columnName: string;
    aliasName: string;
}


export class EngFlowConfig {
    projectId: number;
    folderId: number;
    fileId: number;
    engFlowId: number;
    eventId: number;
    engFlowName: number;
    fileType: number;
}



export class JoinTypes {

    types = [
        {
            value: 'left',
            text: 'Left Join',
        },
        {
            value: 'right',
            text: 'Right Join',
        },
        {
            value: 'inner',
            text: 'Inner Join',
        },
        {
            value: 'full',
            text: 'Full Outer Join',
        }
    ];
}
