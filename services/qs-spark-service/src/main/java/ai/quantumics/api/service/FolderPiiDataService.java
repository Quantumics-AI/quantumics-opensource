/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import ai.quantumics.api.model.QsFoldersPiiData;

public interface FolderPiiDataService {

	QsFoldersPiiData saveFoldersPiiData(QsFoldersPiiData foldersPiiData) throws SQLException;

	List<QsFoldersPiiData> findByFolderId(int folderId);
	
	Optional<QsFoldersPiiData> findByFolderIdAndActive(int folderId);

}
