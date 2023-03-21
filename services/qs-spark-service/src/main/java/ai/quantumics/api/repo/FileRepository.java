/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.QsFiles;

@Repository
public interface FileRepository extends JpaRepository<QsFiles, Integer> {

  List<QsFiles> findByProjectIdAndFolderIdAndActiveTrue(int ProjectId, int folderId);
  
  List<QsFiles> findByProjectIdAndUserIdAndFolderIdAndActiveTrue(int ProjectId, int userId, int folderId);
  
  List<QsFiles> findByProjectIdAndUserIdAndFolderIdAndActiveTrueOrderByCreatedDateAsc(int ProjectId, int userId, int folderId);
  
  List<QsFiles> findByProjectIdAndActiveTrue(int projectId);

  QsFiles findByFileNameAndFolderIdAndActiveTrue(String fileName, int folderId);

  Optional<QsFiles> findByFileIdAndFolderId(int fileId, int folderId);

  QsFiles findByFileName(String fileName);
  
  Optional<QsFiles> findByFileIdAndActiveTrue(int fileId);

  int countByFolderIdAndActiveTrue(int folderId);

  @Modifying
  @Query("update QsFiles file set file.active = false where file.fileId = ?1")
  void updateFileStatus(Integer fileId);

  List<QsFiles> findByProjectIdAndFolderIdIn(int projectId, List<Integer> folderIds);
  
  List<QsFiles> findByProjectIdIn(List<Integer> projectIds);
  
  List<QsFiles> findByProjectId(int projectId);
  
  QsFiles findTopByProjectIdAndUserIdAndFolderIdAndActiveTrueOrderByCreatedDateDesc(int projectId, int userId, int folderId);
}
