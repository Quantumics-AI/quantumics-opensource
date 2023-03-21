export interface GraphParam {
  userId: string;
  projectId: string;
  flowId: number;
}

export interface CreateGraphParam {
  userId: number;
  projectId: number;
  content: string;
  engFlowId: number;
  config: string;
}

export interface PreviewParam {
  projectId: string;
  engFlowId: number;
  eventId: string;
  userId?: number;
}
