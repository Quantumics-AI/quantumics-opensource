package com.qs.api.repo;

import com.qs.api.model.QsFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<QsFiles, Integer> {

    List<QsFiles> findByProjectIdAndFolderId(int ProjectId, int folderId);

    List<QsFiles> findByUserIdAndProjectId(int userId, int projectId);

    QsFiles findByFileNameAndFolderId(String fileName, int folderId);

    QsFiles findByFileName(String fileName);

    int countByFolderId(int folderId);
}
