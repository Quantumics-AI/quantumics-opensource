export interface Project {
    userId: string;
    projectName: string;
    projectDesc: string;
    selectedOutcome: string;
    selectedDataset: any;
    selectedEngineering: any;
    selectedAutomation: any;
    orgName: string;
    schema: string;
    markAsDefault: boolean;
}

export interface UdfData {
    userId: any;
    projectId: any;
    udfName: string;
    // udfDefinition: string;
    udfVersion: any;
    udfIconName: string;
    arguments: [];
    udfId: any;
    udfSyntax: string;
    udfScript: string;
    udfFilePath: string;
    udfScriptLanguage: string;
    udfIconPath: string;
    udf_arguments: string;
    Active: boolean;
    publish: boolean;
    logicfile: any;
    // itemrows: [];
    udfReturnvalue: string;

}
