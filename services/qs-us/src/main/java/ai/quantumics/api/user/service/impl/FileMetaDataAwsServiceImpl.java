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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ai.quantumics.api.user.exceptions.QsRecordNotFoundException;
import ai.quantumics.api.user.model.FileMetaDataAwsRef;
import ai.quantumics.api.user.repo.FileMetaDataAwsRepository;
import ai.quantumics.api.user.req.PreparedDataset;
import ai.quantumics.api.user.service.FileMetaDataAwsService;

@Service
public class FileMetaDataAwsServiceImpl implements FileMetaDataAwsService {

  private final FileMetaDataAwsRepository awsMetaDataRepo;

  public FileMetaDataAwsServiceImpl(FileMetaDataAwsRepository dataSetRefRepoCi) {
    awsMetaDataRepo = dataSetRefRepoCi;
  }

  @Override
  public FileMetaDataAwsRef save(FileMetaDataAwsRef dataSet) {
    return awsMetaDataRepo.save(dataSet);
  }

  @Override
  public void delete(FileMetaDataAwsRef dataSet) {
    awsMetaDataRepo.delete(dataSet);
  }
  
  @Override
  @Transactional
  public Long deleteByPartitionName(String partitionName) {
	  try {
			return awsMetaDataRepo.deleteByTablePartition(partitionName);
		} catch (SQLException sqlException) {
			throw new QsRecordNotFoundException(sqlException.getMessage(), sqlException.getCause());
		}
  }

  @Override
  public void deleteById(long dataSetId) {
    awsMetaDataRepo.deleteById(dataSetId);
  }

  @Override
  public List<FileMetaDataAwsRef> getDistinctFiles(int fileId) {
    return awsMetaDataRepo.findDistinctByFileId(fileId);
  }

  @Override
  public List<FileMetaDataAwsRef> getDistinctFileDesc(int fileId) {
    return awsMetaDataRepo.findTopByFileIdOrderByCreatedDateDesc(fileId);
  }

  @Override
  public List<FileMetaDataAwsRef> getAllByFileId(int fileId) {
    return awsMetaDataRepo.findByFileId(fileId);
  }

  @Override
  @Transactional
  public List<PreparedDataset> getFilesList(int projectId) throws SQLException {
    return awsMetaDataRepo.selectFilesList(projectId);
  }

  @Override
  @Transactional
  public List<FileMetaDataAwsRef> getAllByCreatedBy(String createdBy) {
	return awsMetaDataRepo.findAllByCreatedByOrderByCreatedDateDesc(createdBy);
  }
}
