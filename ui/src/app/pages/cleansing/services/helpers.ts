export const getParentRuleIds = (columns: Array<any>, selectedColumns: Array<string>): string => {
    const strArray = selectedColumns;
    const ruleIds = columns
        .filter(c => strArray.some(sc => sc.toLowerCase() === c.column_name.toLowerCase()) && c.parentRuleId > -1)
        .map(c => c.parentRuleId)
        .join(',');

    return ruleIds;
};
