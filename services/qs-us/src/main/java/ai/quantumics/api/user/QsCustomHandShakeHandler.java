/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user;

import ai.quantumics.api.user.vo.StompPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class QsCustomHandShakeHandler extends DefaultHandshakeHandler {
  @Override
  protected Principal determineUser(
      final ServerHttpRequest request,
      final WebSocketHandler wsHandler,
      final Map<String, Object> attributes) {
    return (Principal) new StompPrincipal(UUID.randomUUID().toString());
  }
}
