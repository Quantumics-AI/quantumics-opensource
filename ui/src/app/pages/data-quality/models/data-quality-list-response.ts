export interface DataQuailtyListResponse {
    id: number;
    ruleName: string;
    ruleDescription: string;
    ruleType: string;
    createdBy: string;
    createdDate: Date;
    modifiedDate: Date;
    status: string;
    selected: boolean;
    ruleId: number;
}