package com.qs.api.service;

import com.qs.api.model.QsFiles;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface FileService {

    QsFiles getFileByNameWithFolderId(String fileName, int folderId);

    List<QsFiles> getFiles(int projectId, int folderId);

    List<QsFiles> getFilesForRedash(int userId, int projectId) throws SQLException;

    Optional<QsFiles> getFileById(int fileId);

    QsFiles getFileByName(String fileName);

    QsFiles saveFileInfo(QsFiles file);

    int getFileCount(int folderId);
}
