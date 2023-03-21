export interface Dictionary {
    id?: number;
    projectId: number;
    folderId: number;
    fileId: number;
    columnName: string;
    dataType: string;
    description: string;
    regularExpression: string;
    pdRegexCode: number;
    example: string;
    dataCustodian: string;
    tags: string;
    published: boolean;
    regexType: string;
}

export interface CommonRegexPatterns {
    active: boolean;
    regexCode: string;
    regexName: string;
    regexPatternId: number;
}

