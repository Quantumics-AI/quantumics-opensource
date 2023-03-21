export class Folder {
    userId: string;
    projectId: string;
    folderName: string;
    folderDesc: string;
    dataOwner: string;
    dataPrepFreq: string;
    folderId?: number;
    file?: File;

    constructor() {
        this.file = new File();
    }
}

export class File {
    fileName: string;
    filePath: string;
}


