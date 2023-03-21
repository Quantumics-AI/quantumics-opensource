/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import ai.quantumics.api.model.EngGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EngGraphRepository extends JpaRepository<EngGraph, Integer> {

  Optional<EngGraph> findByProjectIdAndUserId(int projectId, int userId);

  Optional<EngGraph> findByEngFlowIdAndUserId(int engFlowId, int userId);

  Optional<EngGraph> findByEngGraphIdEquals(int engGraphId);

  Optional<EngGraph> findByProjectIdAndUserIdAndEngFlowId(int projectId, int userId, int engFlowId);

  Optional<EngGraph> findByProjectIdAndEngFlowId(int projectId, int engFlowId);
}
