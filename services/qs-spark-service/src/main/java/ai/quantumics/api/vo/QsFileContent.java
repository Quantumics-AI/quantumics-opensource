/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.vo;

import ai.quantumics.api.model.Projects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
public class QsFileContent {
  int folderId;
  int fileId;
  String folderName;
  String fileName;
  String tableName;
  String partition;
  String fileMetadata;
  String fileObjectKey;
  String fileS3Location;
  String analyticsType;
  Projects project;
}
