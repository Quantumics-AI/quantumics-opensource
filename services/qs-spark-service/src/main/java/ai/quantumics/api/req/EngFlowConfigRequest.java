/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.req;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EngFlowConfigRequest {
	private int projectId;
	private int userId;
	private int engineeringId;
	private List<DataFrameRequest> selectedFiles;
	private List<JoinOperationRequest> selectedJoins;
	private List<AggregateRequest> selectedAggregates;
	private List<UdfOperationRequest> selectedUdfs;
}
