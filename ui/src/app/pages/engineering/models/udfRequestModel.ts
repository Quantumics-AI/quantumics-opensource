export interface UdfRequestModel {
    active: boolean;
    logicfile: string;
    arguments: Arguments[];
    projectId: number;
    publish: boolean;
    udfFilePath?: string;
    udfIconName?: string;
    udfName: string;
    udfReturnvalue?: string;
    udfScript: string;
    udfScriptLanguage: string;
    udfSyntax: string;
    udfVersion: string;
    userId: number;
}

export interface Arguments {
    dataType: string;
    dataValue: string;
    options: string;
}