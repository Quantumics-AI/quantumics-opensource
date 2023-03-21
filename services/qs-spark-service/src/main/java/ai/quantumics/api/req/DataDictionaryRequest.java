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
public class DataDictionaryRequest {
  private int fileId;
  private int folderId;
  private String columnName;
  private String dataType;
  @NotBlank private String description;
  private String example;
  private String regularExpression;
  @NotBlank private String dataCustodian;
  @NotBlank private String tags;
  private boolean published;
  private String createdBy;
  private String regexType;
  private int pdRegexCode;
}
