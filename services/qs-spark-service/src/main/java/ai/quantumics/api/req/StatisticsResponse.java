/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
  "folderCount",
  "filesCount",
  "cleansingJobsCount",
  "dataBaseCount",
  "cleansedFilesCount",
  "engFlowsCount",
  "engJobsCount"
})
public class StatisticsResponse {
  @JsonProperty private int folderCount;
  @JsonProperty private int filesCount;
  @JsonProperty private int cleansingJobsCount;
  @JsonProperty private int dataBaseCount;
  @JsonProperty private int cleansedFilesCount;
  @JsonProperty private int engFlowsCount;
  @JsonProperty private int engJobsCount;
}
