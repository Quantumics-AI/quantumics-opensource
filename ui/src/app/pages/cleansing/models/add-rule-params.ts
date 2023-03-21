export interface AddRuleParams {
    projectId: number;
    fileId: number;
    folderId: number;
    ruleImpactedCols: Array<string>;
    cleansingParamId: number;
    cleansingRuleId: number;
    ruleSequence: number;
    ruleInputLogic: string;
    ruleInputValues: string;
    ruleOutputValues: string;
}
