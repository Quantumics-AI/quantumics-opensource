/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "QSP_Cleansing_Param")
public class CleansingParam {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int cleansingParamId;

  private int cleansingRuleId;
  private int folderId;
  private int fileId;

  private String ruleImpactedCols;
  private String ruleInputValues;
  private String ruleInputLogic;
  private String ruleOutputValues;
  private String ruleDelimiter;
  private int ruleSequence;

  /*
   * private String ruleImpactedCol1; private String ruleImpactedCol2; private String
   * ruleImpactedCol3; private String ruleImpactedCol4; private String ruleImpactedCol5;
   */

  private String ruleInputLogic1;
  private String ruleInputLogic2;
  private String ruleInputLogic3;
  private String ruleInputLogic4;
  private String ruleInputLogic5;

  private String ruleInputValues1;
  private String ruleInputValues2;
  private String ruleInputValues3;
  private String ruleInputValues4;
  private String ruleInputValues5;
  
  private String parentRuleIds;
  
  private Date creationDate;
  private Date modifiedDate;
  
  private String ruleInputNewcolumns;
  private boolean active;
}
