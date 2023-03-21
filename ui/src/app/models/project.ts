export interface Project {
    active: boolean;
    createdBy: string;
    createdDate: string;
    isDeleted: boolean
    projectDesc?: string;
    projectDisplayName: string
    projectId: number
    projectLogo?: string;
    projectMembers?: Array<any>;
    projectName: string;
    subscriptionPlanType: string;
    subscriptionPlanTypeId: number;
    subscriptionStatus: string;
    subscriptionType?: string;
    validDays: number;
    markAsDefault?: boolean;
}
