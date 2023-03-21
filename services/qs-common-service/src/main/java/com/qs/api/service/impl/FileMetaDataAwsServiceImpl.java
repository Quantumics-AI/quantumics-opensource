/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package com.qs.api.service.impl;

import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;
import com.qs.api.model.FileMetaDataAwsRef;
import com.qs.api.repo.FileMetaDataAwsRepository;
import com.qs.api.service.FileMetaDataAwsService;

@Service
public class FileMetaDataAwsServiceImpl implements FileMetaDataAwsService {

  private final FileMetaDataAwsRepository awsMetaDataRepo;

  public FileMetaDataAwsServiceImpl(FileMetaDataAwsRepository dataSetRefRepoCi) {
    awsMetaDataRepo = dataSetRefRepoCi;
  }

  @Override
  public List<FileMetaDataAwsRef> getDistinctFileDesc(int fileId) throws SQLException{
    return awsMetaDataRepo.findTopByFileIdOrderByCreatedDateDesc(fileId);
  }

}
