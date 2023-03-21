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

import ai.quantumics.api.user.exceptions.QsRecordNotFoundException;
import ai.quantumics.api.user.model.FileMetaDataAwsRef;
import ai.quantumics.api.user.req.PreparedDataset;

public interface FileMetaDataAwsService {

  FileMetaDataAwsRef save(FileMetaDataAwsRef dataSet) throws SQLException;

  void delete(FileMetaDataAwsRef dataSet) throws SQLException;

  void deleteById(long dataSetId) throws SQLException;

  List<FileMetaDataAwsRef> getDistinctFiles(int fileId);

  List<FileMetaDataAwsRef> getDistinctFileDesc(int fileId);

  List<FileMetaDataAwsRef> getAllByFileId(int fileId) throws SQLException;

  List<PreparedDataset> getFilesList(int projectId) throws SQLException;
  
  List<FileMetaDataAwsRef> getAllByCreatedBy(String createdBy);

  Long deleteByPartitionName(String partitionName) throws QsRecordNotFoundException;
}
