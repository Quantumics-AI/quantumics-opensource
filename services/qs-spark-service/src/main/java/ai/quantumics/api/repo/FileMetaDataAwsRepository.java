/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import java.sql.SQLException;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ai.quantumics.api.model.FileMetaDataAwsRef;
import ai.quantumics.api.req.PreparedDataset;

@Repository
public interface FileMetaDataAwsRepository extends JpaRepository<FileMetaDataAwsRef, Long> {

  List<FileMetaDataAwsRef> findByFileId(int fileId);

  List<FileMetaDataAwsRef> findDistinctByFileId(int fileId);

  List<FileMetaDataAwsRef> findTopByFileIdOrderByCreatedDateDesc(int fileId);

  @Modifying
  @Query(
      "select new ai.quantumics.api.req.PreparedDataset(re.athenaTable,re.tablePartition, re.fileType, re.fileMetaData, re.createdDate, re.createdBy, fi.fileId, fi.fileName, fl.folderId, fl.folderName,  fl.projectId) from  FileMetaDataAwsRef re "
          + "join QsFiles fi on fi.fileId = re.fileId join QsFolders fl on fl.folderId = fi.folderId "
          + "join RunJobStatus run_job on re.fileId = cast(run_job.fileId As integer) "
          + "WHERE fl.projectId = :projectId and fl.active = True and fi.active = True")
  List<PreparedDataset> selectFilesList(@Param("projectId") int projectId);
  
  List<FileMetaDataAwsRef> findAllByCreatedByOrderByCreatedDateDesc(String createdBy);
  
  Long deleteByTablePartition(String partitionName) throws SQLException;
  
  
  
  
}
