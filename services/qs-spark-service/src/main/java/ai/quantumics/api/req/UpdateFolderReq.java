/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.req;

import lombok.Data;

@Data
public class UpdateFolderReq {
  /*
   * private String folderName; //TODO S3 FolderName CANT BE MODIFIED private String folderDesc;
   * private String dataOwner; private String dataPrepFreq; private String cleansingRuleSetName;
   */
  
  private String folderDisplayName;
  private String folderDesc;
}
