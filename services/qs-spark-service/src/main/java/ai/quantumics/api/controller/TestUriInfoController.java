/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.TestUriInfo;
import ai.quantumics.api.service.TestUriInfoService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import springfox.documentation.spring.web.json.Json;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class TestUriInfoController {
  private final ControllerHelper helper;
  private final TestUriInfoService uriInfoService;

  public TestUriInfoController(ControllerHelper helperCi, TestUriInfoService uriInfoServiceCi) {
    helper = helperCi;
    uriInfoService = uriInfoServiceCi;
  }

  @ApiOperation(value = "unit test cases information", response = Json.class)
  @GetMapping("/api/v1/unittestcasesinfo")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Get BucketName")})
  public ResponseEntity<Object> allTestUriInfo() {
    Map<String, Object> response = new HashMap<>();
    try {
      helper.swtichToPublicSchema();
      List<TestUriInfo> testUrls = uriInfoService.getAll();
      log.info("found number of records {}", testUrls.size());
      if (testUrls.isEmpty()) {
        response =
            helper.createSuccessMessage(
                HttpStatus.NO_CONTENT.value(), "Found Zero Records", testUrls);
      } else {
        response =
            helper.createSuccessMessage(
                HttpStatus.OK.value(), "All Resources Information", testUrls);
      }
    } catch (SQLException exception) {
      log.error("Error while reading resources information {}", exception.getMessage());
      helper.failureMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());
    }
    return ResponseEntity.ok().body(response);
  }
}
