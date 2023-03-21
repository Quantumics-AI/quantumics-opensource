/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.RulesCatalogue;
import ai.quantumics.api.repo.RulesCatalogueRepository;
import ai.quantumics.api.service.RulesCatalogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RulesCatalogueServiceImpl implements RulesCatalogueService {
  @Autowired private RulesCatalogueRepository catalogueRepo;

  @Override
  public RulesCatalogue getRulesDefinitionByName(String ruleName) {
    return catalogueRepo.findByRuleName(ruleName);
  }
}
