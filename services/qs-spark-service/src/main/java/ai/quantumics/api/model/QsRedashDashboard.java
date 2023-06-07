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
import java.util.Date;

@Entity
@Table(name = "qsp_redash_dashboard")
@Data
public class QsRedashDashboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rdId;
    private String rdKey;
    private String dashboardName;

    private int dashboardId;

    private Date creationDate;

    private boolean active;


}
