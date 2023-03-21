/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.req;

import lombok.Data;

import javax.validation.constraints.Min;
import java.util.ArrayList;

@Data
public class UpdateProjectRequest {
  private int userId;

  private String displayName;
  private String description;
  private String selectedOutcome;
  private ArrayList<String> selectedDataset;
  private ArrayList<String> selectedEngineering;
  private ArrayList<String> selectedAutomation;
  private boolean markAsDefault;
}
