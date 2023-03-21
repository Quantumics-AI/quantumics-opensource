/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.EngGraph;
import ai.quantumics.api.repo.EngGraphRepository;
import ai.quantumics.api.service.EngGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class EngGraphServiceImpl implements EngGraphService {
  @Autowired private EngGraphRepository enggRepo;

  @Override
  @Transactional
  public Optional<EngGraph> getContent(final int projectId, final int userId) throws SQLException {
    return enggRepo.findByProjectIdAndUserId(projectId, userId);
  }

  @Override
  public Optional<EngGraph> getGraphXml(int engFlowId, int userId) throws SQLException {
    return enggRepo.findByEngFlowIdAndUserId(engFlowId, userId);
  }

  @Override
  public Optional<EngGraph> isGraphIdPresent(int engGraphId) throws SQLException {
    return enggRepo.findByEngGraphIdEquals(engGraphId);
  }

  @Override
  public Optional<EngGraph> getRecordForEngFlowId(int projectId, int engFlowId) {
    return enggRepo.findByProjectIdAndEngFlowId(projectId, engFlowId);
  }

  @Override
  public EngGraph save(final EngGraph state) throws SQLException {
    return enggRepo.save(state);
  }

  @Override
  public void deleteGraph(int engGraphId) throws SQLException {
    enggRepo.deleteById(engGraphId);
  }
}
