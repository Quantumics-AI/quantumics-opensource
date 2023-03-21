/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "qs_rules_catalog")
public class RulesCatalogue {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int ruleDefId;

  @Column(columnDefinition = "TEXT", unique = true)
  private String ruleName;

  @Column(columnDefinition = "TEXT")
  private String ruleContents;

  private int userId;
  private Date createdDate;
  private Date modifiedDate;
  private String createdBy;
  private String modifiedBy;
}
