export interface Database {
    name: string;
    metaData: MetaData[];
    isClick: boolean;
}

export interface DatabasePayload {
    pipelineName: string;
    connectorName: string;
    connectorConfig: DbConnectorConfig;
    connectorType: string;
    projectId: number;
    userId: number;
    schemaName: string;
    tableName: string;
    sql: string;
    sqlType: string;
    isExistingpipeline: boolean;
    connectorId: number;
    pipelineId: number;
}

export interface DbConnectorConfig {
    hostName: string;
    userName: string;
    password: string;
    databaseName: string;
    port: string;
    serviceName: string;
}

export interface TableSelection {
    tableName: string;
    schemaName: string;
    isClick: boolean;
}

export interface MetaData {
    name: string;
}