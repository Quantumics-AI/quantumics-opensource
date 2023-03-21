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
import java.util.Optional;

import ai.quantumics.api.user.model.RunJobStatus;

public interface RunJobService {

  RunJobStatus saveRunJob(RunJobStatus runJobStatus);
  
  void deleteCleanseJob(int runJobId);

  Optional<RunJobStatus> getRunJob(int runJobId) throws SQLException;

  List<RunJobStatus> getAllJobs(int userId, int projectId) throws SQLException;

  List<RunJobStatus> getAllFilesHistory(int projectId, int folderId);

  List<RunJobStatus> getCleanseFilesByProjectId(int projectId) throws SQLException;

  int getSucceededJobsOnly(int userId, int projectId) throws SQLException;

  int getSucceededJobsOnlyInFolder(int userId, int projectId, int folderId) throws SQLException;
  
  List<RunJobStatus> getSucceededJobsInFolder(int userId, int projectId, int folderId) throws SQLException;
  
}
