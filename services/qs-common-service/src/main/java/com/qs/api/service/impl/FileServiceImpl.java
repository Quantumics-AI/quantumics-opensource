package com.qs.api.service.impl;

import com.qs.api.model.QsFiles;
import com.qs.api.repo.FileRepository;
import com.qs.api.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileRepository fileRepo;

    @Override
    public QsFiles saveFileInfo(QsFiles file) {
        return fileRepo.save(file);
    }

    @Override
    public List<QsFiles> getFiles(int projectId, int folderId) {
        return fileRepo.findByProjectIdAndFolderId(projectId, folderId);
    }

    @Override
    public List<QsFiles> getFilesForRedash(int userId, int projectId) {
        return fileRepo.findByUserIdAndProjectId(userId, projectId);
    }

    @Override
    public Optional<QsFiles> getFileById(int fileId) {
        return fileRepo.findById(fileId);
    }

    @Override
    public QsFiles getFileByName(String fileName) {
        return fileRepo.findByFileName(fileName);
    }

    @Override
    public QsFiles getFileByNameWithFolderId(String fileName, int folderId) {
        return fileRepo.findByFileNameAndFolderId(fileName, folderId);
    }

    @Override
    public int getFileCount(int folderId) {
        return fileRepo.countByFolderId(folderId);
    }
}
