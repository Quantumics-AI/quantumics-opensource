/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.QsRedashDashboard;
import ai.quantumics.api.repo.RedashDashboardRepository;
import ai.quantumics.api.service.RedashDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class RedashDashboardServiceImpl implements RedashDashboardService {

    @Autowired
    private RedashDashboardRepository redashDashboardRepository;


    @Override
    public QsRedashDashboard saveDashboard(QsRedashDashboard qsRedashDashboard) throws SQLException {
        return redashDashboardRepository.saveAndFlush(qsRedashDashboard);
    }

    @Override
    public QsRedashDashboard getDashboardForDashboardIdAndRdKey(int dashboardId, String rdKey) throws SQLException {
        return redashDashboardRepository.findByDashboardIdAndRdKeyAndActiveTrue(dashboardId, rdKey);
    }

    @Override
    public List<QsRedashDashboard> getAllDashboardsForUserRedashKey(String rdKey) throws SQLException {
        return redashDashboardRepository.findByRdKeyAndActiveTrue(rdKey);
    }

    @Override
    public List<QsRedashDashboard> getAllDashboards() throws SQLException {
        return redashDashboardRepository.findAllByActiveTrue();
    }
}
