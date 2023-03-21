/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class CorrelationInterceptor implements HandlerInterceptor {
  private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
  private static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

  @Override
  public void afterCompletion(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Object handler,
      final Exception ex)
      throws Exception {
    MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
  }

  private String generateUniqueCorrelationId() {
    return UUID.randomUUID().toString();
  }

  private String getCorrelationIdFromHeader(final HttpServletRequest request) {
    String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
    if (StringUtils.isEmpty(correlationId)) {
      correlationId = generateUniqueCorrelationId();
    }
    return correlationId;
  }

  @Override
  public boolean preHandle(
      final HttpServletRequest request, final HttpServletResponse response, final Object handler)
      throws Exception {
    final String correlationId = getCorrelationIdFromHeader(request);
    MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
    return true;
  }
}
