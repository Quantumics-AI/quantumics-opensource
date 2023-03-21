/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.QsPartition;
import ai.quantumics.api.repo.PartitionRepository;
import ai.quantumics.api.service.PartitionService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class PartitionServiceImpl implements PartitionService {
  private final PartitionRepository partitionRepo;

  public PartitionServiceImpl(PartitionRepository partitionRepoCi) {
      partitionRepo = partitionRepoCi;
  }

  @Override
  public Optional<QsPartition> getPartition(final int partitionId) throws SQLException {
    return partitionRepo.findById(partitionId);
  }

  @Override
  public Optional<QsPartition> getPartitionByFileName(
      final String folderName, final String fileName) throws SQLException {
    return partitionRepo.findByFolderNameAndFileName(folderName, fileName);
  }

  @Override
  public Optional<QsPartition> getPartitionByFileId(int folderId, int fileId) throws SQLException {
    return partitionRepo.findByFolderIdAndFileId(folderId, fileId);
  }

  @Override
  public Optional<QsPartition> getPartitionDetails(final int projectId, final int folderId)
      throws SQLException {
    return partitionRepo.findByProjectIdAndFolderId(projectId, folderId);
  }

  @Override
  public Optional<QsPartition> getPartitions(final int folderId, final int fileId)
      throws SQLException {
    return partitionRepo.findByFolderIdAndFileId(folderId, fileId);
  }
  
  @Override
  public List<QsPartition> getPartitionsOfFolder(int folderId) throws SQLException {
    return partitionRepo.findByFolderId(folderId);
  }

  @Override
  public QsPartition save(final QsPartition qsPartition) throws SQLException {
    return partitionRepo.save(qsPartition);
  }
}
