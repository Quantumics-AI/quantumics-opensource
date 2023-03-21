package ai.quantumics.api.service;

import java.sql.SQLException;
import ai.quantumics.api.model.FileDataProfilingInfo;

public interface FileDataProfileService {

    FileDataProfilingInfo getFileDataProfilingInfo(int projectId, int folderId, int fileId) throws SQLException;

    FileDataProfilingInfo save(FileDataProfilingInfo fileDataProfileInfo) throws SQLException;

}
