/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package com.qs.api.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.qs.api.model.RunJobStatus;

@Repository
public interface RunJobRepository extends JpaRepository<RunJobStatus, Integer> {

  List<RunJobStatus> findByUserIdAndProjectId(int userId, int projectId);

  List<RunJobStatus> findByProjectId(int projectId);

  List<RunJobStatus> findByProjectIdAndFolderId(int projectId, int folderId);

  List<RunJobStatus> findDistinctByProjectIdAndFolderId(int projectId, int FolderId);

  int countByUserIdAndProjectIdAndStatus(int userId, int projectId, String status);
  int countByUserIdAndProjectIdAndFolderIdAndStatus(int userId, int projectId, int folderId, String status);
}
