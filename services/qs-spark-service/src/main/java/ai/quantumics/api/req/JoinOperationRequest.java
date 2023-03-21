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

import javax.validation.constraints.NotBlank;


@Data
@NoArgsConstructor
public class JoinOperationRequest {
  private int projectId;
  private int engFlowId;
  private int eventId;
  // private String uniqueId;
  private String joinName;
  private String joinType;
  // private int firstFileId;
  // private int secondFileId;
  private int eventId1;
  private int eventId2;
  private String dfId;
  // private String joinOperations; Service B will update it already
  @NotBlank private String secondFileColumn;
  @NotBlank private String firstFileColumn;
  
  private FileJoinColumnMetadata metadata;
}
