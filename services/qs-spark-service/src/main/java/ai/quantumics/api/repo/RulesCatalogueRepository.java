/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import ai.quantumics.api.model.RulesCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RulesCatalogueRepository extends JpaRepository<RulesCatalogue, Integer> {

  RulesCatalogue findByRuleName(String ruleName);
}
