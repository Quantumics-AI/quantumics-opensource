/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

  @Override
  public void doFilter(
      final ServletRequest servletRequest,
      final ServletResponse servletResponse,
      final FilterChain chain)
      throws IOException, ServletException {

    final HttpServletRequest request = (HttpServletRequest) servletRequest;
    final HttpServletResponse resp = (HttpServletResponse) servletResponse;
    //inal String clientOrigin = request.getHeader("Origin");

    /*if (clientOrigin != null) {
      resp.setHeader("Access-Control-Allow-Origin", clientOrigin);
    } else {*/
      resp.setHeader("Access-Control-Allow-Origin", "*"); // TODO 'SELF'
    //}

    resp.setHeader(
        "Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD, OPTIONS, UPGRADE");
    resp.setHeader("Access-Control-Max-Age", "3600");
    resp.setHeader("Access-Control-Allow-Headers", "*");
    resp.setHeader("Access-Control-Allow-Credentials", "true");

    if (request.getMethod().equals("OPTIONS")) {
      System.out.println(" OPTIONS METHOD CALLED");
      resp.setStatus(HttpServletResponse.SC_ACCEPTED);
      return;
    }
    chain.doFilter(request, servletResponse);
  }
}
