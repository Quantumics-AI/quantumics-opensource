/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.req;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonPropertyOrder({"projectId","engFlowId", "eventId", "dataFrameEventId", "groupByColumns", "columns", "groupByColumnsMetaData", "columnsMetaData"})
public class AggregateRequest {
  
  @JsonProperty
  private int projectId;
  
  @JsonProperty
  private int engFlowId;
  
  @JsonProperty 
  private int eventId;

  @JsonProperty
  private int dataFrameEventId;

  @JsonProperty("groupByColumns")
  private List<String> groupByColumns;

  @JsonProperty("columns")
  private List<AggReqColumn> columns;

  @JsonProperty("groupByColumnsMetaData")
  private List<Metadata> groupByColumnsMetaData;
  
  @JsonProperty("columnsMetaData")
  private List<AggReqColumn> columnsMetaData;
}
