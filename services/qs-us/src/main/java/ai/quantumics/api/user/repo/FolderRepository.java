/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.repo;

import ai.quantumics.api.user.model.QsFolders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<QsFolders, Integer> {

  List<QsFolders> findByProjectIdAndActiveTrue(int projectId);

  QsFolders findByFolderNameAndActiveTrue(String folderName);

  List<QsFolders> findByuserIdAndActiveTrue(int userId);
  
  List<QsFolders> findByProjectIdAndUserIdAndActiveTrue(int projectId, int userId);

  QsFolders findByFolderIdAndActive(int folderId, boolean active);

  Optional<QsFolders> findByFolderNameEqualsAndActive(String folderName, boolean active);

  boolean existsQsFoldersByFolderName(String folderName);

  int countByUserIdAndProjectId(int userId,int projectId);

  List<QsFolders> findByUserIdAndProjectIdAndActiveOrderByCreatedDateDesc(int userId, int projectId, boolean active);
  
  List<QsFolders> findByUserIdAndProjectIdAndIsExternalAndActiveOrderByCreatedDateDesc(int userId, int projectId, boolean isExternal,boolean active);
  
  //Optional<QsFolders> findByFolderDisplayNameIgnoreCaseOrFolderNameIgnoreCaseAndActive(String folderDisplayName, String folderName, boolean active);
  
  List<QsFolders> findByFolderDisplayNameIgnoreCaseOrFolderNameIgnoreCaseAndActive(String folderDisplayName, String folderName, boolean active);
  
  QsFolders findByFolderIdAndProjectIdAndUserIdAndActiveTrue(int folderId, int projectId,
      int userId);
  
  List<QsFolders> findByProjectIdIn(List<Integer> projectIds);
  
  List<QsFolders> findByProjectId(int projectId);
}
