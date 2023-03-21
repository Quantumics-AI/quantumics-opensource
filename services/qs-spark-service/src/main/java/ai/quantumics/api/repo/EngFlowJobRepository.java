/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ai.quantumics.api.model.EngFlowJob;

@Repository
public interface EngFlowJobRepository extends CrudRepository<EngFlowJob, Integer> {
  EngFlowJob findByEngFlowId(int engFlowId);
  
  List<EngFlowJob> findByEngFlowIdAndStatus(int engFlowId, String status);
  
  List<EngFlowJob> findByEngFlowIdInAndStatusOrderByEngFlowIdDesc(List<Integer> engFlowId, String status);

  int countByEngFlowIdAndStatus(int engFlowId, String status);
  
  int countByEngFlowIdIn(List<Integer> engFlowIds);
  
}
