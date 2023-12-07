export interface CleansingJobs {
    folderId: number;
    userId: number;
    projectId: number;
    fileId: number;
    folderName: string;
    folderDesc: string;
    dataOwner: string;
    dataPrepFreq: string;
    cleansingRuleSetName: string;
    createdBy: string;
    modifiedBy: string;
    createdDate: string;
    modifiedDate: string;
    parentId: number;
    columnHeaders: string;
    active: boolean;
    folderDisplayName: string;
    filesCount: number;
    pipelineName: string;
    pipelineType: string;
    external: boolean;
}