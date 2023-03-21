/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.req;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class DataBaseRequest {

  private int userId;
  private int projectId;
  private int connectorId;
  private String connectorName;
  @JsonProperty("connectorConfig")
  private Map<String, String> connectorConfig;
  private String connectorType;
  private int pipelineId;
  private String pipelineName;
  private String pipelineType;
  private String schemaName;
  private String tableName;
  private String sql;
  private String sqlType;
  private boolean saveDbCon; 
  @JsonProperty("piiData")
  private List<PiiData> piiData;
  
  
}
 