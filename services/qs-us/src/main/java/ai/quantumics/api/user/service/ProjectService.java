/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import ai.quantumics.api.user.model.Projects;
import ai.quantumics.api.user.vo.UserInfo;

public interface ProjectService {
  
  String PROJECT_MEMBERS_QUERY = "select upj.userId AS userId, uv2.userRole AS userRole, upr.userFirstName AS firstName,"
      + "upr.userLastName AS lastName, uv2.userEmail AS email,"
      + "upr.userPhone AS phone, upr.userCompany AS company, upr.userCountry AS country from QsUserProjects upj "
      + "join QsUserProfileV2 upr on upj.userId = upr.userId  "
      + "join QsUserV2 uv2 "
      + "on upr.userId = uv2.userId "
      + "where upj.projectId = :projectId";

  List<Projects> getProjectsForUser(int userId) throws SQLException;

  String getSchemaNameFromUser(int userId);

  Projects saveProject(Projects project);

  Projects getProject(int projectId);
  
  Projects getProject(int projectId, int userId);
  
  Optional<Projects> getProjectOp(int projectId);

  String getSchemaName(int projectId);

  List<Projects> getEmAll();

  List<UserInfo> getProjectMembers(int projectId);
  
  List<Projects> getDeletedProjects();
  
  boolean deleteProjects(List<Projects> deletedProjects);
}
