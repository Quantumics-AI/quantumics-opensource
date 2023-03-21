/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.EngFlow;
import ai.quantumics.api.repo.EngFlowRepository;
import ai.quantumics.api.service.EngFlowService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class EngFlowServiceImpl implements EngFlowService {
  private final EngFlowRepository engFlowRepo;

  public EngFlowServiceImpl(EngFlowRepository engFlowRepo) {
    this.engFlowRepo = engFlowRepo;
  }

  @Override
  public EngFlow save(EngFlow engFlow) throws SQLException {
    return engFlowRepo.save(engFlow);
  }

  @Override
  public EngFlow saveIfNotPresent(EngFlow engFlow) throws SQLException {
    return null;
  }

  @Override
  public List<EngFlow> getFlowsForProjectAndFlowName(int projectId, String flowName)
      throws SQLException {
    return engFlowRepo.findByProjectIdAndEngFlowName(projectId, flowName);
  }

  @Override
  public Optional<EngFlow> getFlowsForProjectAndId(int projectId, int engFlowId)
      throws SQLException {
    return engFlowRepo.findByProjectIdAndEngFlowId(projectId, engFlowId);
  }

  @Override
  public List<EngFlow> getFlowForUser(int userId, String flowName) throws SQLException {
    return engFlowRepo.findByUserIdAndEngFlowName(userId, flowName);
  }

  @Override
  public List<EngFlow> getFlowForUserFromFlow(int userId, int engFlowId) throws SQLException {
    return engFlowRepo.findByUserIdAndEngFlowId(userId, engFlowId);
  }

  @Override
  public List<EngFlow> getFlowNames(int projectId, int userId) throws SQLException {
    return engFlowRepo.findByProjectIdAndUserIdAndParentEngFlowIdAndActiveTrue(projectId, userId, 0);
  }
  
  @Override
  public List<EngFlow> getFlowNames(int projectId) throws SQLException {
    return engFlowRepo.findByProjectIdAndParentEngFlowId(projectId, 0);
  }

  @Override
  public List<EngFlow> getFlowNamesDescOrder(int projectId) throws SQLException {
    return engFlowRepo.findAllByProjectIdAndParentEngFlowIdAndActiveOrderByCreatedDateDesc(projectId, 0, true);
  }

  @Override
  public List<EngFlow> getChildEngFlows(int projectId, int parentEngFlowId) throws SQLException {
    return engFlowRepo.findByProjectIdAndParentEngFlowId(projectId, parentEngFlowId);
  }
  
  @Override
  public List<EngFlow> getRecentChildEngFlow(int projectId, int parentEngFlowId) throws SQLException {
    return engFlowRepo.findAllByProjectIdAndParentEngFlowIdAndActiveOrderByEngFlowIdDesc(projectId, parentEngFlowId, true);
  }

  @Override
  public void deleteEngFlow(int engFlowId) throws SQLException {
    engFlowRepo.deleteById(engFlowId);
  }

  @Override
  public void deleteEngFlow(String engFlowName) throws SQLException {
    engFlowRepo.deleteByEngFlowName(engFlowName);
  }

  @Override
  public void updateFlowStatus(int engFlowId) throws SQLException {
    engFlowRepo.updateFlowStatus(engFlowId);
  }

  @Override
  public List<EngFlow> getAllActiveEngFlowsForProject(int projectId) throws SQLException {
    return engFlowRepo.findAllByProjectIdAndParentEngFlowIdAndActiveOrderByCreatedDateDesc(projectId, 0, true);
  }
  
  @Override
  public boolean isExists(String engFlowName) throws SQLException {
    return engFlowRepo.existsByEngFlowNameIgnoreCaseAndActive(engFlowName, true);
  }
}
