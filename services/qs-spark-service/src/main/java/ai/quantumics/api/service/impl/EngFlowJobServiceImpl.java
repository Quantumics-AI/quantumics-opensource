/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.EngFlowJob;
import ai.quantumics.api.repo.EngFlowJobRepository;
import ai.quantumics.api.service.EngFlowJobService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class EngFlowJobServiceImpl implements EngFlowJobService {

  private final EngFlowJobRepository flowJobRepository;
  private String completed = "SUCCEEDED";

  public EngFlowJobServiceImpl(EngFlowJobRepository flowJobRepository) {
    this.flowJobRepository = flowJobRepository;
  }

  @Override
  public EngFlowJob save(EngFlowJob engFlowJob) {
    return flowJobRepository.save(engFlowJob);
  }
  
  @Override
  public void saveAll(List<EngFlowJob> engFlowJobs) {
    flowJobRepository.saveAll(engFlowJobs);
  }

  @Override
  public EngFlowJob getJobByEngFlowId(int engFlowId) {
    return flowJobRepository.findByEngFlowId(engFlowId);
  }

  @Override
  public Optional<EngFlowJob> getThisJob(int engFlowJobId) {
    return flowJobRepository.findById(engFlowJobId);
  }
  
  @Override
  public List<EngFlowJob> getCompletedJobsByEngFlowId(int engFlowId) {
    return flowJobRepository.findByEngFlowIdAndStatus(engFlowId, completed);
  }
  
  @Override
  public List<EngFlowJob> getCompletedJobsByEngFlowIds(List<Integer> engFlowIds) {
    return flowJobRepository.findByEngFlowIdInAndStatusOrderByEngFlowIdDesc(engFlowIds, completed);
  }

  @Override
  public int getCompletedJobsForFlow(int engFlowId) throws SQLException {
    return flowJobRepository.countByEngFlowIdAndStatus(engFlowId, completed);
  }
  
  @Override
  public EngFlowJob getCompletedJobByEngFlowId(int engFlowId) {
    
    // This returns only one record, need to see why we need to return a list and process it again.
    List<EngFlowJob> flowJobs = flowJobRepository.findByEngFlowIdAndStatus(engFlowId, completed); 
    
    if(flowJobs != null && !flowJobs.isEmpty()) {
      return flowJobs.get(0);
    }
    return null;
  }
  
  @Override
  public long countOfJobs() throws SQLException {
    return flowJobRepository.count();
  }
  
  @Override
  public long countOfJobs(List<Integer> engFlowIds) throws SQLException {
    return flowJobRepository.countByEngFlowIdIn(engFlowIds);
  }
  
}
