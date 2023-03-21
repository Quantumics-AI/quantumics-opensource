/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "QSP_Cleansing_Rule")
@Data
public class CleansingRule {
  @Id
  @SequenceGenerator(
      name = "cleanseRuleSequence",
      sequenceName = "QspCleanseRuleSequence",
      initialValue = 1,
      allocationSize = 5)
  @GeneratedValue(generator = "cleanseRuleSequence")
  private int cleansingRuleId;

  private int folderId;
  private String ruleName;
  private String ruleTechDesc;
  private int sequence;
}
