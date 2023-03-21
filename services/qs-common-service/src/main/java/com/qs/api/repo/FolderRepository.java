package com.qs.api.repo;

import com.qs.api.model.QsFolders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<QsFolders, Integer> {

    List<QsFolders> findByProjectId(int projectId);

    QsFolders findByFolderName(String folderName);

    List<QsFolders> findByuserId(int userId);

    QsFolders findByFolderId(int folderId);

    Optional<QsFolders> findByFolderNameEquals(String folderName);

    boolean existsQsFoldersByFolderName(String folderName);
}
