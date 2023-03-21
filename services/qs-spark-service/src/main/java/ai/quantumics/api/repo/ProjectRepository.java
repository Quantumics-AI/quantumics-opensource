/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.Projects;

@Repository
public interface ProjectRepository extends JpaRepository<Projects, Long> {

  Optional<Projects> findByProjectId(int projectId);
  
  Optional<Projects> findByProjectIdAndUserId(int projectId, int userId);

  List<Projects> findByUserIdAndActiveTrue(int userId);

  Optional<Projects> findByUserIdAndMarkAsDefaultTrue(int userId);
  
  List<Projects> findByDeletionDateNotNull();

  boolean existsByProjectNameAndActiveTrue(String projectName);
}
