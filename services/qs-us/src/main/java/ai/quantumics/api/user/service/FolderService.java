/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package ai.quantumics.api.user.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import ai.quantumics.api.user.model.QsFolders;

public interface FolderService {

  QsFolders saveFolders(QsFolders folders) throws SQLException;

  List<QsFolders> getFoldersForUser(int userId);
  
  List<QsFolders> getFoldersInProjectForUser(int projectId, int userId);

  QsFolders getFolderByName(String folderName);

  List<QsFolders> getFolders(int projectId);

  QsFolders getFolder(int folderId);

  Optional<QsFolders> isExists(String folderName) throws SQLException;

  int countFolders(int userId, int projectId) throws SQLException;

  List<QsFolders> allFolders(int userId, int projectId) throws SQLException;
  
  List<QsFolders> allFolders(int userId, int projectId,boolean isExternal) throws SQLException;

  List<QsFolders> saveFolderCollection(List<QsFolders> foldersList) throws SQLException;
  
  boolean isFolderNameAvailable(String folderdisplayName) throws SQLException;
  
  QsFolders getActiveFolder(int folderId, int projectId, int userId);
  
  List<QsFolders> getFoldersToDelete(List<Integer> projectIds);
  
  List<QsFolders> getFoldersToDelete(int projectId);
  
  boolean deleteFolders(List<QsFolders> folderToDelete);
}
