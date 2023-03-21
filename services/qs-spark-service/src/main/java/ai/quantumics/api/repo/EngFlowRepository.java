/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import ai.quantumics.api.model.EngFlow;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EngFlowRepository extends CrudRepository<EngFlow, Integer> {

  List<EngFlow> findByProjectIdAndEngFlowName(int projectId, String flowName);

  Optional<EngFlow> findByProjectIdAndEngFlowId(int projectId, int engFlowId);

  List<EngFlow> findByUserIdAndEngFlowName(int userId, String flowName);

  List<EngFlow> findByUserIdAndEngFlowId(int userId, int engFlowId);

  List<EngFlow> findByProjectIdAndUserIdAndParentEngFlowIdAndActiveTrue(int projectId, int userId, int parentEngFlowId);

  List<EngFlow> findByProjectIdAndUserIdAndParentEngFlowIdOrderByEngFlowIdDesc(int projectId, int userId, int parentEngFlowId);
  
  List<EngFlow> findAllByProjectIdAndParentEngFlowIdAndActiveOrderByEngFlowIdDesc(int projectId, int parentEngFlowId, boolean active);
  
  List<EngFlow> findByProjectIdAndParentEngFlowId(int projectId, int parentEngFlowId);

  Optional<EngFlow> findByEngFlowName(String flowName);

  List<EngFlow> findAllByProjectIdAndParentEngFlowIdAndActiveOrderByCreatedDateDesc(int projectId, int parentEngFlowId, boolean active);

  void deleteByEngFlowName(String engFlowName);

  @Modifying
  @Query("update EngFlow e set e.active = false where e.engFlowId = ?1")
  void updateFlowStatus(int engFlowId);
  
  boolean existsByEngFlowNameIgnoreCaseAndActive(String engFlowName, boolean active);
}
