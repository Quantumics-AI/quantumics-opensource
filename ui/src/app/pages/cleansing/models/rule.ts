export interface Rule {
    cleansingParamId: number;
    parentRuleIds: string;
    ruleSequence: number;
    ruleImpactedCols: string;

    ruleInputValues: string;
    ruleInputValues1: string;
    ruleInputValues2: string;
    ruleInputValues3: string;
    ruleInputValues4: string;
    ruleInputValues5: string;

    ruleInputLogic: string;
    ruleInputLogic1: string;
    ruleInputLogic2: string;
    ruleInputLogic3: string;
    ruleInputLogic4: string;
    ruleInputLogic5: string;
    ruleOutputValues: string;

    ruleDelimiter: string;

    numberOfPreviewColumns?: number;

}

export interface Column {
    parentRuleId?: number;
    column_name: string;
    data_type: string;
    preview: 'new' | 'old' | '';
}

