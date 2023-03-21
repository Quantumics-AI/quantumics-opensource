/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.service.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ai.quantumics.api.user.model.QsFiles;
import ai.quantumics.api.user.repo.FileRepository;
import ai.quantumics.api.user.service.FileService;

@Service
public class FileServiceImpl implements FileService {

  private final FileRepository fileRepo;

  public FileServiceImpl(FileRepository fileRepo) {
    this.fileRepo = fileRepo;
  }

  @Override
  public QsFiles saveFileInfo(QsFiles file) {
    return fileRepo.saveAndFlush(file);
  }

  @Override
  public List<QsFiles> getFiles(int projectId, int folderId) {
    return fileRepo.findByProjectIdAndFolderIdAndActiveTrue(projectId, folderId);
  }
  
  @Override
  public List<QsFiles> getFiles(int projectId, int userId, int folderId) {
    return fileRepo.findByProjectIdAndUserIdAndFolderIdAndActiveTrue(projectId, userId, folderId);
  }
  
  @Override
  public QsFiles getOldestFileInFolder(int projectId, int userId, int folderId) {
    List<QsFiles> files = fileRepo.findByProjectIdAndUserIdAndFolderIdAndActiveTrueOrderByCreatedDateAsc(projectId, userId, folderId);
    QsFiles file = null;
    if(files != null && !files.isEmpty()) {
      file = files.get(0);
    }
    
    return file;
  }
  
  @Override
  public List<QsFiles> getAllActiveFiles(int projectId) {
    return fileRepo.findByProjectIdAndActiveTrue(projectId);
  }

  @Override
  public List<QsFiles> getFilesForRedash(int userId, int projectId) {
    return fileRepo.findByUserIdAndProjectIdAndActiveTrue(userId, projectId);
  }

  @Override
  public Optional<QsFiles> getFileById(int fileId) {
    return fileRepo.findByFileIdAndActiveTrue(fileId);
  }

  @Override
  public QsFiles getFileByName(String fileName) {
    return fileRepo.findByFileName(fileName);
  }

  @Override
  public QsFiles getFileByNameWithFolderId(String fileName, int folderId) {
    return fileRepo.findByFileNameAndFolderIdAndActiveTrue(fileName, folderId);
  }

  @Override
  public Optional<QsFiles> getFileIdWithFolderId(int fileId, int folderId) throws SQLException {
    return fileRepo.findByFileIdAndFolderId(fileId, folderId);
  }

  @Override
  public int getFileCount(int folderId) {
    return fileRepo.countByFolderIdAndActiveTrue(folderId);
  }
  
  @Override
  public List<QsFiles> saveFilesCollection(List<QsFiles> filesList) throws SQLException {
    return fileRepo.saveAll(filesList);
  }
  
  @Override
  public List<QsFiles> getFilesForFolderIds(int projectId, List<Integer> folderId) {
    return fileRepo.findByProjectIdAndFolderIdIn(projectId,folderId);
  }
  
  @Override
  public List<QsFiles> getFiles(List<Integer> projectIds) {
    return fileRepo.findByProjectIdIn(projectIds);
  }
  
  @Override
  public List<QsFiles> getFiles(int projectId) {
    return fileRepo.findByProjectId(projectId);
  }
  
  @Override
  public boolean deleteFiles(List<QsFiles> files) {
    try {
      fileRepo.deleteAll(files);
      
      return true;
    }catch(Exception e) {
      return false;
    }
  }
  
  @Override
  public QsFiles getLatestFileInFolder(int projectId, int userId, int folderId) throws SQLException {
	  return fileRepo.findTopByProjectIdAndUserIdAndFolderIdAndActiveTrueOrderByCreatedDateDesc(projectId, userId, folderId);
  }
}
