/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import ai.quantumics.api.model.RunJobStatus;
import ai.quantumics.api.vo.CleansedJobResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunJobRepository extends JpaRepository<RunJobStatus, Integer> {

  List<RunJobStatus> findByUserIdAndProjectId(int userId, int projectId);

  List<RunJobStatus> findByProjectId(int projectId);

  List<RunJobStatus> findByProjectIdAndFolderId(int projectId, int folderId);

  List<RunJobStatus> findByProjectIdAndFolderIdAndStatus(int projectId, int folderId, String status);

  List<RunJobStatus> findDistinctByProjectIdAndFolderId(int projectId, int FolderId);

  int countByUserIdAndProjectIdAndStatus(int userId, int projectId, String status);
  
  int countByUserIdAndProjectIdAndFolderIdAndStatus(int userId, int projectId, int folderId, String status);
  
  List<RunJobStatus> findByUserIdAndProjectIdAndFolderIdAndStatus(int userId, int projectId, int folderId, String status);

  @Query("SELECT rjs FROM RunJobStatus rjs " +
          "WHERE rjs.userId = :userId AND rjs.projectId = :projectId " +
          "AND EXISTS (SELECT 1 FROM RunJobStatus subRjs " +
          "            WHERE subRjs.folderId = rjs.folderId " +
          "            AND subRjs.userId = :userId AND subRjs.projectId = :projectId " +
          "            GROUP BY subRjs.folderId " +
          "            HAVING MAX(subRjs.createdDate) = rjs.createdDate) " +
          "ORDER BY rjs.createdDate DESC")
  List<RunJobStatus> findLatestRunJobStatusPerFolderForUserAndProjectId(int userId, int projectId);

  @Modifying
  @Query(" SELECT new ai.quantumics.api.vo.CleansedJobResponse("+
          "    f.folderId as folderId, f.userId, f.projectId, f.folderName, f.folderDisplayName,"+
          "    f.createdBy, f.createdDate, f.modifiedBy, f.modifiedDate, f.isExternal , f.dataOwner, "+
          "    (SELECT r1.status FROM RunJobStatus r1"+
          "    WHERE r1.folderId = f.folderId"+
          "    AND r1.fileId = r.fileId"+
          "    AND r1.createdDate = ("+
          "    SELECT MAX(r2.createdDate) FROM RunJobStatus r2"+
          "    WHERE r2.folderId = f.folderId"+
          "     AND r2.fileId = r.fileId"+
          "  )) AS jobStatus, "+
          "  r.createdDate as jobCreatedDate)"+
          " from QsFolders f "+
          " JOIN RunJobStatus r on r.folderId = f.folderId "+
          " JOIN QsFiles fl on f.folderId = fl.folderId and fl.active = true "+
          " WHERE f.active = true "+
          " AND f.userId = :userId "+
          " AND f.projectId = :projectId "+
          " AND r.createdDate = ("+
          " SELECT MAX(createdDate)"+
          " FROM RunJobStatus "+
          " WHERE folderId = f.folderId"+
          " AND fileId = r.fileId"+
          " ) "+
          " ORDER BY f.modifiedDate DESC")
  List<CleansedJobResponse> getPreparationJobFolders(@Param("userId") int userId, @Param("projectId") int projectId);

}
