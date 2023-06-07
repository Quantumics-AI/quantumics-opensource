/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service;

import ai.quantumics.api.model.QsRedashDashboard;

import java.sql.SQLException;
import java.util.List;

public interface RedashDashboardService {

    QsRedashDashboard saveDashboard(QsRedashDashboard qsRedashDashboard) throws SQLException;

    QsRedashDashboard getDashboardForDashboardIdAndRdKey(int dashboardId, String rdKey) throws SQLException;

    List<QsRedashDashboard> getAllDashboardsForUserRedashKey(String rdKey) throws SQLException;

    List<QsRedashDashboard> getAllDashboards() throws SQLException;


}
