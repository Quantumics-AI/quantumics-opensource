/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.CleansingRule;
import ai.quantumics.api.repo.CleansingRuleRepostiory;
import ai.quantumics.api.service.CleansingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CleansingRuleServiceImpl implements CleansingRuleService {

  @Autowired CleansingRuleRepostiory cleanseRuleRepo;

  @Override
  public CleansingRule saveRule(CleansingRule cleansingRule) {
    return cleanseRuleRepo.saveAndFlush(cleansingRule);
  }

  @Override
  public List<CleansingRule> getRulesForFolder(int folderId) {
    return cleanseRuleRepo.findByFolderId(folderId);
  }

  @Override
  public Optional<CleansingRule> getRuleById(int cleansingRuleId) {
    return cleanseRuleRepo.findById(cleansingRuleId);
  }
}
