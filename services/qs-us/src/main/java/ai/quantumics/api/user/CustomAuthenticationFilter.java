/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationFilter implements Filter {

  @Override
  public void doFilter(
      final ServletRequest servletRequest,
      final ServletResponse servletResponse,
      final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    final String uri = httpRequest.getRequestURL().toString();
    final String Auth = httpRequest.getHeader("Authorization");
    if (isContains(uri)
        || isContains(uri, "/authorization")
        || isContains(uri, "/awsLanding")
        || isContains(uri, "/health")
        || uri.endsWith(".html")
        || isContains(uri, "actuator")
        || isContains(uri, "/wsinit")
        || uri.endsWith("api-docs")
        || isContains(uri, "/stripe/events/webhook")) {
      log.debug("Intercepter for URL --  {}", uri);
      chain.doFilter(servletRequest, servletResponse);
    } else if (!StringUtils.isEmpty(Auth)) {
      log.debug("Interceptor Found Auth Header! {}", Auth);
      chain.doFilter(httpRequest, servletResponse);
    } else {
      ((HttpServletResponse) servletResponse).setHeader("Content-Type", "application/json");
      ((HttpServletResponse) servletResponse).setStatus(401);
      servletResponse
          .getOutputStream()
          .write(
              "UnAuthorized Operation, you are not allowed to perform this operation".getBytes());
      return;
    }
  }

  private boolean isContains(final String uri) {
    return isContains(uri, "/users");
  }

  private boolean isContains(final String uri, final String s) {
    return uri.contains(s);
  }
}
