package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.FileDataProfilingInfo;
import ai.quantumics.api.repo.FileDataProfilingRepository;
import ai.quantumics.api.service.FileDataProfileService;

@Service
public class FileDataProfileServiceImpl implements FileDataProfileService {

    FileDataProfilingRepository fileDataProfileRepo;

    public FileDataProfileServiceImpl(FileDataProfilingRepository fileDataProfileRepo) {
        this.fileDataProfileRepo = fileDataProfileRepo;
    }

    @Override
    public FileDataProfilingInfo getFileDataProfilingInfo(int projectId, int folderId, int fileId) throws SQLException{
        return fileDataProfileRepo.findByProjectIdAndFolderIdAndFileId(projectId, folderId, fileId);
    }

    @Override
    public FileDataProfilingInfo save(FileDataProfilingInfo fileDataProfileInfo) throws SQLException {
        return fileDataProfileRepo.save(fileDataProfileInfo);
    }

}
