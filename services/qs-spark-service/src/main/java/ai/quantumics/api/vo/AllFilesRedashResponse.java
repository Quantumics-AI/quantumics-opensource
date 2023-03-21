/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonPropertyOrder({
  "projectId",
  "rawFilesResponse",
  "cleansedFilesResponse",
  "engineeredFilesResponse"
})
public class AllFilesRedashResponse {

  @JsonProperty private int projectId;

  @JsonProperty private List<RawFilesResponse> rawFilesResponse;

  @JsonProperty private List<CleanseFilesRedashResponse> cleansedFilesResponse;

  @JsonProperty private List<EngineeredFilesRedashResponse> engineeredFilesResponse;
}
