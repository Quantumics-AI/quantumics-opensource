export interface notification {
    adminMsg: boolean;
    creationDate: string;
    notificationId: number;
    notificationMsg: string;
    notificationRead: boolean;
    projectId: number;
    userId: number;
    isTruncated?: boolean;
    toolTipText?: string;
}
