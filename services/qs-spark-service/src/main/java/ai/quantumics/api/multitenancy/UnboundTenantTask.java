/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.multitenancy;

import java.util.concurrent.Callable;

import static ai.quantumics.api.constants.QsConstants.QS_DEFAULT_TENANT_ID;

public abstract class UnboundTenantTask<T> implements Callable<T> {

  @Override
  public T call() throws Exception {
    TenantContext.setCurrentTenant(QS_DEFAULT_TENANT_ID);
    return callInternal();
  }

  protected abstract T callInternal();
}
