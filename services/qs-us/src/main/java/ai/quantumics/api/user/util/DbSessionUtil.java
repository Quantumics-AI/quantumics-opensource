/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.util;

import ai.quantumics.api.user.constants.QsConstants;
import ai.quantumics.api.user.multitenancy.TenantContext;
import ai.quantumics.api.user.service.DatabaseSessionManager;

import org.springframework.stereotype.Component;

@Component
public class DbSessionUtil {

  private final DatabaseSessionManager sessionManager;

  public DbSessionUtil(DatabaseSessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  /** @param schemaName */
  public void changeSchema(final String schemaName) {
    sessionManager.unbindSession();
    TenantContext.setCurrentTenant(schemaName);
    sessionManager.bindSession();
  }

  public boolean isPublicSchema() {
    return QsConstants.PUBLIC.equals(TenantContext.getCurrentTenant());
  }
}
