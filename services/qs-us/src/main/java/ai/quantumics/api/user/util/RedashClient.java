/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.util;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class RedashClient {

  @Value("${qs.redash.user.url}")
  private String redashUrl;

  @Autowired private RestTemplate restTemplate;

  public String createRedashUser(final String userName, final String email, final String password) {
    log.info("Request recieve for {},{},{}", userName, email, password);
    String jsonStr = null;
    HttpStatus statusCode = null;
    final JsonObject payload = new JsonObject();
    payload.addProperty("username", userName);
    payload.addProperty("email", email);
    payload.addProperty("password", password);
    ResponseEntity<String> postForEntity = null;
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    final HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);
    try {
      log.info("Redash user url : {}" + redashUrl);
      log.info("Request Redash to create a new user {}", payload);
      postForEntity = restTemplate.postForEntity(redashUrl, entity, String.class);
      statusCode = postForEntity.getStatusCode();
      log.info("Status code : {}" + statusCode);
      if (statusCode.is2xxSuccessful()) {
        log.debug("POSTENTITY {}", postForEntity);
        jsonStr = postForEntity.getBody();
        // Process the body from redash and give the token
      }
      log.info("Redash User created sucessfully : {} and status was {}", jsonStr, statusCode);
      return jsonStr;
    } catch (final Exception e) {
      log.error("Exception: establishing a session {}", e.getMessage());
      return jsonStr;
    }
  }

  public String createRedashRootUser() {
    String jsonStr = null;
    HttpStatus statusCode = null;
    ResponseEntity<String> getForEntity = null;
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String redashRootUrl = redashUrl + "/root";
    try {
      log.info("Request Redash to create a root user ");
      log.info("Root user url : {}" + redashRootUrl);
      getForEntity = restTemplate.getForEntity(redashRootUrl, String.class);
      statusCode = getForEntity.getStatusCode();
      log.info("statusCode : " + statusCode);
      if (statusCode.is2xxSuccessful()) {
        log.debug("POSTENTITY {}", getForEntity);
        jsonStr = getForEntity.getBody();
        // Process the body from redash and give the token
        log.info("Redash Root User created sucessfully and returned 200 status");
      }
      log.info("Redash Root User created sucessfully : {} and status was {}", jsonStr, statusCode);
      return jsonStr;
    } catch (final Exception e) {
      log.error("Exception: establishing a session {}", e.getMessage());
      return jsonStr;
    }
  }
}
