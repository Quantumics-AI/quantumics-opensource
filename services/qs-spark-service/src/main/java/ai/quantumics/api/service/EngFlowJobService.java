/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service;

import ai.quantumics.api.model.EngFlowJob;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EngFlowJobService {

  EngFlowJob save(EngFlowJob engFlowJob);
  
  void saveAll(List<EngFlowJob> engFlowJobs);

  EngFlowJob getJobByEngFlowId(int engFlowId);

  Optional<EngFlowJob> getThisJob(int engFlowJobId);
  
  List<EngFlowJob> getCompletedJobsByEngFlowId(int engFlowId);
  
  List<EngFlowJob> getCompletedJobsByEngFlowIds(List<Integer> engFlowIds);
  
  EngFlowJob getCompletedJobByEngFlowId(int engFlowId);
  
  int getCompletedJobsForFlow(int engFlowId) throws SQLException;
  
  long countOfJobs() throws SQLException;
  
  long countOfJobs(List<Integer> engFlowIds) throws SQLException;
}
