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
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.qs.api.model.RunJobStatus;
import com.qs.api.repo.RunJobRepository;
import com.qs.api.service.RunJobService;

@Service
public class RunJobServiceImpl implements RunJobService {

  private final String SUCCEEDED = "SUCCEEDED";
  private final RunJobRepository runJobRepo;

  public RunJobServiceImpl(RunJobRepository runJobRepo) {
    this.runJobRepo = runJobRepo;
  }

  @Override
  public RunJobStatus saveRunJob(RunJobStatus runJobStatus) {
    return runJobRepo.saveAndFlush(runJobStatus);
  }
  
  @Override
  public void deleteCleanseJob(int runJobId) {
    runJobRepo.deleteById(runJobId);
  }

  @Override
  public Optional<RunJobStatus> getRunJob(int runJobId) throws SQLException {
    return runJobRepo.findById(runJobId);
  }

  @Override
  public List<RunJobStatus> getAllJobs(int userId, int projectId) throws SQLException {
    return runJobRepo.findByUserIdAndProjectId(userId, projectId);
  }

  @Override
  public List<RunJobStatus> getAllFilesHistory(int projectId, int folderId) {
    return runJobRepo.findByProjectIdAndFolderId(projectId, folderId);
  }

  @Override
  @Transactional
  public List<RunJobStatus> getCleanseFilesByProjectId(int projectId) throws SQLException {
    return runJobRepo.findByProjectId(projectId);
  }

  @Override
  public int getSucceededJobsOnly(int userId, int projectId) throws SQLException {
    return runJobRepo.countByUserIdAndProjectIdAndStatus(userId, projectId, SUCCEEDED);
  }

  @Override
  public int getSucceededJobsOnlyInFolder(int userId, int projectId, int folderId)
      throws SQLException {
    return runJobRepo.countByUserIdAndProjectIdAndFolderIdAndStatus(
        userId, projectId, folderId, SUCCEEDED);
  }
}
