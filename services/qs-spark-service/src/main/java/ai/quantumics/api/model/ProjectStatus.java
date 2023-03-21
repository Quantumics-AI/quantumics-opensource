/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.model;

import lombok.Data;

@Data
public class ProjectStatus {
  private String bucket;
  private String raw_db;
  private String processed_db;
  private String raw_crawler;
  private String processed_crawle;
  private String schema;
  private String qs_project_table;

  public ProjectStatus() {
      bucket = "Success";
      raw_db = "Success";
      processed_db = "Success";
      raw_crawler = "Success";
      processed_crawle = "Success";
      schema = "Success";
      qs_project_table = "Success";
  }
}
