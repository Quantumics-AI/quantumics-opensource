/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "qsp_home_kpi")
@Data
public class QsHomeKPI {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int kpiId;
    private String kpiDetails;
    private int userId;
}
