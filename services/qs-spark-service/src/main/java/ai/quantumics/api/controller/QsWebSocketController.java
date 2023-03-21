/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.livy.LivyActions;
import ai.quantumics.api.model.EngFlow;
import ai.quantumics.api.model.EngRunFlowRequest;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.req.EngFlowConfigRequest;
import ai.quantumics.api.req.QsSubscriptionRequest;
import ai.quantumics.api.service.EngFlowService;
import ai.quantumics.api.vo.QsSocketAck;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Slf4j
//@Controller
public class QsWebSocketController {

  private final ControllerHelper helper;
  private final LivyActions livyActions;
  private final EngFlowService engFlowService;
  private final SimpMessageSendingOperations messagingTemplate;

  public QsWebSocketController(
      final ControllerHelper helperCi,
      final LivyActions livyActionsCi,
      final SimpMessageSendingOperations messagingTemplateCi,
      final EngFlowService engFlowServiceCi) {
    helper = helperCi;
    livyActions = livyActionsCi;
    messagingTemplate = messagingTemplateCi;
    engFlowService = engFlowServiceCi;
  }

  @MessageExceptionHandler
  @SendToUser("/queue/errors")
  public String handleException(final Throwable exception) {
    return exception.getMessage();
  }

  @SubscribeMapping("/topic/engg")
  public QsSocketAck initialSubscription(final QsSubscriptionRequest msg) {
    log.info("Subscription Request {}", msg.getContent());
    return new QsSocketAck(msg.getUniqueId(), "Subscription Activated");
  }

  @MessageMapping("/qswsrun")
  @Deprecated
  public void runEngineeringFlow(EngRunFlowRequest runFlowRequest) {
    log.info("Engineering Operation requested for {}", runFlowRequest.toString());
    try {
      Projects projects = helper.getProjects(runFlowRequest.getProjectId());

      // Get the Engineering Flow Config column value using the EngFlowId and parse that information
      Optional<EngFlow> content =
          engFlowService.getFlowsForProjectAndId(
              runFlowRequest.getProjectId(), runFlowRequest.getEngFlowId());

      if (content.isPresent()) {
        EngFlow engFlow = content.get();
        String engFlowConfigJson = engFlow.getEngFlowConfig();
        log.info("Engineering flow config payload used for processing is: {}", engFlowConfigJson);
        messagingTemplate.convertAndSend(
            "/topic/engg", "Process started please monitor the results in automation screen!");

        // Process the configuration JSON and prepare the information in the DataFrameRequest format
        // for further processing...
        Gson gson = new Gson();
        EngFlowConfigRequest engFlowConfigRequest =
            gson.fromJson(engFlowConfigJson, EngFlowConfigRequest.class);

        int jobId =
            helper.runEngJobFileOpHandler(
                engFlowConfigRequest.getSelectedFiles(), projects, runFlowRequest.getEngFlowId(), runFlowRequest.getFlowJobId(), null);

        helper.runEngJobJoinOpHandler(
            engFlowConfigRequest.getSelectedJoins(),
            projects,
            runFlowRequest.getEngFlowId(),
            jobId, null);

      } else {
        messagingTemplate.convertAndSend(
            "/topic/errors", "Error occurred while processing the selected files.");
      }

    } catch (Exception exception) {
      log.error("Message -- {}", exception.getMessage());
    }
  }
}
