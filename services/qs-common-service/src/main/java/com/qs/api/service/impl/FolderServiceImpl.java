package com.qs.api.service.impl;

import com.qs.api.model.QsFolders;
import com.qs.api.repo.FolderRepository;
import com.qs.api.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class FolderServiceImpl implements FolderService {

    @Autowired
    private FolderRepository folderRepo;

    @Override
    public QsFolders saveFolders(QsFolders folders) throws SQLException {
        return folderRepo.saveAndFlush(folders);
    }

    @Override
    public List<QsFolders> getFolders(int projectId) {
        return folderRepo.findByProjectId(projectId);
    }

    @Override
    public List<QsFolders> getFoldersForuser(int userId) {
        return folderRepo.findByuserId(userId);
    }

    @Override
    public QsFolders getFolder(int folderId) {
        return folderRepo.findByFolderId(folderId);
    }

    @Override
    public Optional<QsFolders> isExists(String folderName) throws SQLException {
        return folderRepo.findByFolderNameEquals(folderName);
    }

    @Override
    public QsFolders getFolderByName(String folderName) {
        return folderRepo.findByFolderName(folderName);
    }
}
