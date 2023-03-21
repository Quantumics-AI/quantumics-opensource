package ai.quantumics.api.repo;

import java.sql.SQLException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.FileDataProfilingInfo;

@Repository
public interface FileDataProfilingRepository extends JpaRepository<FileDataProfilingInfo, Integer> {

    FileDataProfilingInfo findByDataProfileId(int dataProfileId) throws SQLException;

    FileDataProfilingInfo findByProjectIdAndFolderIdAndFileId(int projectId, int folderId, int fileId) throws SQLException;

}
