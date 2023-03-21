/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadDataRequest {

  private int port;
  private String dbName;
  private String userName;
  private String password;
  private String dbType; /* Oracle,MySql,MS-SQL or PostgreSql */
  private String hostName;
  private String serviceName;
  private int projectId;
  private int folderId;
  private int userId;
  private String tableName;
}
