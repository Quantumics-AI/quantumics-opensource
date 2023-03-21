/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import ai.quantumics.api.model.QsPartition;

public interface PartitionService {

  Optional<QsPartition> getPartition(int partitionId) throws SQLException;

  Optional<QsPartition> getPartitionByFileName(String folderName, String fileName)
      throws SQLException;
  
  Optional<QsPartition> getPartitionByFileId(int folderId, int fileId)
	      throws SQLException;

  Optional<QsPartition> getPartitionDetails(int projectId, int folderId) throws SQLException;

  Optional<QsPartition> getPartitions(int folderId, int fileId) throws SQLException;
  
  List<QsPartition> getPartitionsOfFolder(int folderId) throws SQLException;

  QsPartition save(QsPartition qsPartition) throws SQLException;
}
