import { Folder } from "./folder";

export class Pii {
    column: string;
    isPII: boolean;
    piiMaskActionType?: string;
}

export class PiiData {
    TABLE_NAME: string;
    CSV_FILE_PATH: string;
    PII_COLUMNS_INFO: string;
    code: string;
    message: string;
    Folder_Id: string;
}


export class PiiDataResult {
    folder: Folder;
    TableName: string;
    CSV_FILE_PATH: string;
    pii: Array<Pii>;
}
