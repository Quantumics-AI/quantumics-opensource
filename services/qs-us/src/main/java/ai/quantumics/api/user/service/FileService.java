/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import ai.quantumics.api.user.model.QsFiles;

public interface FileService {

  QsFiles getFileByNameWithFolderId(String fileName, int folderId);

  Optional<QsFiles> getFileIdWithFolderId(int fileId, int folderId) throws SQLException;

  List<QsFiles> getFiles(int projectId, int folderId);
  
  List<QsFiles> getFiles(int projectId, int userId, int folderId);
  
  QsFiles getOldestFileInFolder(int projectId, int userId, int folderId);
  
  List<QsFiles> getAllActiveFiles(int projectId);

  List<QsFiles> getFilesForRedash(int userId, int projectId) throws SQLException;

  Optional<QsFiles> getFileById(int fileId);

  QsFiles getFileByName(String fileName);

  QsFiles saveFileInfo(QsFiles file);

  int getFileCount(int folderId);
  
  List<QsFiles> saveFilesCollection(List<QsFiles> filesList) throws SQLException;
  
  List<QsFiles> getFilesForFolderIds(int projectId, List<Integer> folderId);
  
  List<QsFiles> getFiles(List<Integer> projectIds);
  
  List<QsFiles> getFiles(int projectId);
  
  boolean deleteFiles(List<QsFiles> files);
  
  QsFiles getLatestFileInFolder(int projectId, int userId, int folderId) throws SQLException;
}
