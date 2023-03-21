/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.multitenancy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContext {

  private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

  public static String getCurrentTenant() {
    return currentTenant.get();
  }

  public static void setCurrentTenant(String tenant) {
    log.debug("Setting tenant to " + tenant);
    currentTenant.set(tenant);
  }

  public static void clear() {
    currentTenant.set(null);
  }
}
