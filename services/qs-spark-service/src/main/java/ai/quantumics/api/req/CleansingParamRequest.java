/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.req;

import lombok.Data;

import java.util.ArrayList;

@Data
public class CleansingParamRequest {
  private int cleansingParamId;
  private int cleansingRuleId;
  private int folderId;
  private int fileId;
  private ArrayList<String> ruleImpactedCols;
  private String ruleInputValues;
  private String ruleInputValues1;
  private String ruleInputValues2;
  private String ruleInputValues3;
  private String ruleInputValues4;
  private String ruleInputValues5;
  
  private String ruleInputLogic;
  
  private String ruleOutputValues;
  private String ruleDelimiter;
  
  private String ruleInputLogic1;
  private String ruleInputLogic2;
  private String ruleInputLogic3;
  private String ruleInputLogic4;
  private String ruleInputLogic5;

  private int ruleSequence;
  
  private String parentRuleIds;
  
  private String ruleInputNewcolumns;
  private boolean active;
}
