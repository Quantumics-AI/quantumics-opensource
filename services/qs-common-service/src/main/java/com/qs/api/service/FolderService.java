package com.qs.api.service;

import com.qs.api.model.QsFolders;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface FolderService {

    QsFolders saveFolders(QsFolders folders) throws SQLException;

    List<QsFolders> getFoldersForuser(int userId);

    QsFolders getFolderByName(String folderName);

    List<QsFolders> getFolders(int projectId);

    QsFolders getFolder(int folderId);

    Optional<QsFolders> isExists(String folderName) throws SQLException;
}
