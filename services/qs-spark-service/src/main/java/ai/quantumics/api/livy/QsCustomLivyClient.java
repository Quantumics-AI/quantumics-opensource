/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.livy;

import static java.lang.Thread.sleep;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ai.quantumics.api.model.EngFlowEvent;
import ai.quantumics.api.service.EngFlowEventService;
import ai.quantumics.api.vo.BatchJobLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class QsCustomLivyClient {

  private final Gson gson = new Gson();
  private final RestTemplate restTemplate = new RestTemplate();
  
  private final EngFlowEventService engFlowEventService;
  private int livySessionId = -1;

  @Value("${qs.livy.base.url}")
  private String livyBaseUrl;
  
  @Value("${qs.livy.base.batches.url}")
  private String livyBaseBatchesUrl;
  
  @Value("${qs.livy.batch.job.max.timeout.period}")
  private long batchJobMaxTimeoutPeriod;
  
  @Value("${spark.executorMemory}")
  private String executorMemory;
  
  @Value("${spark.driverMemory}")
  private String driverMemory;
  
  public QsCustomLivyClient(EngFlowEventService engFlowEventService) {
    this.engFlowEventService = engFlowEventService;
  }

  public void deleteActiveSession() {
    final int sessionId = getLivySessionId();
    final String url = livyBaseUrl + sessionId;
    try {
      restTemplate.delete(url);
      setLivySessionId(-1);
      log.info("Active session had been deleted");
    } catch (final Exception e) {
      log.error("Exception while deleting session {}", e.getMessage());
      throw e;
    }
  }
  
  public boolean deleteLivySession(int sid) {
    final String url = livyBaseUrl + sid;
    boolean status = false;
    try {
      restTemplate.delete(url);
      log.info("Livy session with Id: {} had been deleted.", sid);
      status = true;
    } catch (final Exception e) {
      log.error("Exception while deleting session {}", e.getMessage());
      throw e;
    }
    
    return status;
  }
  
  public boolean deleteLivyBatchJob(int bid) {
    final String url = livyBaseBatchesUrl + bid;
    boolean status = false;
    try {
      restTemplate.delete(url);
      log.info("Livy Batch Job with Id: {} had been deleted.", bid);
      status = true;
    } catch (final Exception e) {
      log.error("Exception while deleting batch Job {}", e.getMessage());
      throw e;
    }
    
    return status;
  }
  
  public int getOrCreateApacheLivySessionId(final String sessionName) {
    final String url = livyBaseUrl;
    HttpStatus statusCode;
    JsonNode sessionResNode;
    final JsonObject payload = new JsonObject();
    payload.addProperty("kind", "pyspark");
    payload.addProperty("name", sessionName);
    payload.addProperty("executorMemory", executorMemory);
    payload.addProperty("driverMemory", driverMemory);
    int sessionId = -1;
    ObjectMapper mapper = new ObjectMapper();
    ResponseEntity<String> getResp;
    try {
      getResp = restTemplate.getForEntity(url, String.class);
      statusCode = getResp.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        final String jsonStr = getResp.getBody();
        
        sessionResNode = mapper.readValue(jsonStr, JsonNode.class);
        JsonNode sessions = sessionResNode.get("sessions");
        Iterator<JsonNode> sessionsIter = sessions.iterator();
        
        boolean flag = false;
        while(sessionsIter.hasNext()) {
          JsonNode session = sessionsIter.next();
          String sn = session.get("name").asText();
          if(sn != null && sessionName.equals(sn)) {
            // Session is already present. No need to create a new one..
            sessionId = session.get("id").asInt();
            flag = true;
            
            break;
          }
        }
        
        if(!flag) {
          // No session found with the session name hence creating a new one..
          sessionId = createNewApacheLivySession(mapper, payload.toString());
        }
      }else {
        // If control comes here, it means session is not present, we have to create a new
        // session based on the name...
        sessionId = createNewApacheLivySession(mapper, payload.toString());
      }
    } catch (final Exception e) {
      log.info("Exception reading active sessions {}", e.getMessage());
    }
    
    return sessionId;
  }
  
  public int getApacheLivySessionIdByName(final String sessionName) {
    final String url = livyBaseUrl;
    HttpStatus statusCode;
    JsonNode sessionResNode;
    final JsonObject payload = new JsonObject();
    payload.addProperty("kind", "pyspark");
    payload.addProperty("name", sessionName);
    payload.addProperty("executorMemory", executorMemory);
    payload.addProperty("driverMemory", driverMemory);
    int sessionId = -1;
    ObjectMapper mapper = new ObjectMapper();
    ResponseEntity<String> getResp;
    try {
      getResp = restTemplate.getForEntity(url, String.class);
      statusCode = getResp.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        final String jsonStr = getResp.getBody();
        
        sessionResNode = mapper.readValue(jsonStr, JsonNode.class);
        JsonNode sessions = sessionResNode.get("sessions");
        Iterator<JsonNode> sessionsIter = sessions.iterator();
        
        while(sessionsIter.hasNext()) {
          JsonNode session = sessionsIter.next();
          String sn = session.get("name").asText();
          if(sn != null && sessionName.equals(sn)) {
            // Session is already present. No need to create a new one..
            sessionId = session.get("id").asInt();
            
            break;
          }
        }
      }
    } catch (final Exception e) {
      log.info("Exception reading active sessions {}", e.getMessage());
    }
    
    return sessionId;
  }
  
  private int createNewApacheLivySession(ObjectMapper mapper, String payload) throws Exception{
    final String postResp = livyPostHandler(livyBaseUrl, payload);
    JsonNode sessionPostResNode = mapper.readValue(postResp, JsonNode.class);
    int sessionId = sessionPostResNode.get("id").asInt();
    
    return sessionId;
  }

  public int getApacheLivySessionId(final String sessionName) {
    int retVal = 0;
    final String url = livyBaseUrl;
    HttpStatus statusCode;
    JsonElement elementTemp;
    final JsonObject payload = new JsonObject();
    payload.addProperty("kind", "pyspark");
    payload.addProperty("name", sessionName);
    payload.addProperty("executorMemory", executorMemory);
    payload.addProperty("driverMemory", driverMemory);
    ResponseEntity<String> postForEntity;
    log.info("Initialize the session with - {}", payload.toString());
    if (livySessionId == -1) {
      try {
        postForEntity = restTemplate.getForEntity(url, String.class);
        statusCode = postForEntity.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
          final String jsonStr = postForEntity.getBody();
          elementTemp = gson.fromJson(jsonStr, JsonElement.class);
          final JsonObject jsonObj = elementTemp.getAsJsonObject();
          final JsonArray asJsonArray = jsonObj.get("sessions").getAsJsonArray();
          log.info("JSON Size of Response List: {}", asJsonArray.size());
          if (asJsonArray.size() >= 1) {
            elementTemp = asJsonArray.get(0).getAsJsonObject().get("id");
            log.info("Current active sessions available & id was {}", elementTemp.toString());
            retVal = Integer.parseInt(elementTemp.toString());
          } else {
            log.info("Currently No active sessions available");
            log.info("Reconnecting .... ");
            final String apacheLivyPost = livyPostHandler(url, payload.toString());
            elementTemp = gson.fromJson(apacheLivyPost, JsonElement.class);
            elementTemp = elementTemp.getAsJsonObject().get("id");
            retVal = Integer.parseInt(elementTemp.toString());
            log.info("New session ID - {}", retVal);
          }
          setLivySessionId(retVal);
        }
      } catch (final Exception e) {
        log.info("Exception reading active sessions {}", e.getMessage());
      }
    } else {
      retVal = livySessionId;
    }
    return retVal;
  }

  public String getApacheLivySessionLog(final int sessionId) {
    String jsonStr = null;
    HttpStatus statusCode;
    final String url = livyBaseUrl + sessionId + "/log";
    ResponseEntity<String> postForEntity;
    try {
      postForEntity = restTemplate.getForEntity(url, String.class);
      statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        jsonStr = postForEntity.getBody();
      }
    } catch (final Exception e) {
      log.error("Exception: reading session log {}", e.getMessage());
    }
    return jsonStr;
  }

  public String getApacheLivySessionState(final int sessionId) {
    final String url = livyBaseUrl + sessionId + "/state";
    String jsonStr = null;
    JsonElement fromJson;
    HttpStatus statusCode;
    ResponseEntity<String> postForEntity;
    final String idle = LivySessionState.idle.toString();
    LivySessionState.success.toString();
    String state = LivySessionState.not_started.toString();
    try {
      while (!state.equals(idle)) {
        postForEntity = restTemplate.getForEntity(url, String.class);
        statusCode = postForEntity.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
          jsonStr = postForEntity.getBody();
          fromJson = gson.fromJson(jsonStr, JsonElement.class);
          state = fromJson.getAsJsonObject().get("state").getAsString();
        }
        sleep(1000);
      }
    } catch (final Exception e) {
      setLivySessionId(-1);
      log.error("Global Session updated to default value");
      log.error("Exception: reading session state {}", e.getMessage());
    }
    return jsonStr;
  }
  
  public JsonNode getApacheLivyBatchState(final int batchId, final ObjectMapper mapper) {
    final String url = livyBaseBatchesUrl + batchId;
    String jsonStr = null;
    HttpStatus statusCode;
    ResponseEntity<String> postForEntity;
    JsonNode node = null;
    final String success = LivySessionState.success.toString();
    final String dead = LivySessionState.dead.toString();
    String state = LivySessionState.starting.toString();
    try {
      Timer timer = new Timer();
      SimpleTimerTask task = new SimpleTimerTask(batchJobMaxTimeoutPeriod);
      timer.schedule(task, 0);
      
      while (!(state.equals(success) || state.equals(dead))) {
        postForEntity = restTemplate.getForEntity(url, String.class);
        statusCode = postForEntity.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
          jsonStr = postForEntity.getBody();
          node = mapper.readValue(jsonStr, JsonNode.class);
          state = node.get("state").asText();
        }

        sleep(2000);
        
        if(task.isElapsed()) {
          // Abort the Batch Job as it is in hanged state for more than 5mins..
          log.info("Deleting the batch job as it is in hanged state for more than: {} msecs.", batchJobMaxTimeoutPeriod);
          deleteLivyBatchJob(batchId);
        }
      }
      
      // Stop the timer as the task is complete...
      timer.cancel();
      
      return node;
    } catch (final Exception e) {
      log.error("Exception: reading batch state {}", e.getMessage());
    }
    return null;
  }
  
  public List<String> getApacheLivyBatchJobLog(final int batchId, final ObjectMapper mapper) {
    final String url = livyBaseBatchesUrl + batchId+"/log";
    String jsonStr = null;
    ResponseEntity<String> postForEntity;
    BatchJobLog bcl = null;
    
    try {
      postForEntity = restTemplate.getForEntity(url, String.class);
      if (postForEntity != null) {
        jsonStr = postForEntity.getBody();
        bcl = mapper.readValue(jsonStr, BatchJobLog.class);
        if(bcl != null) {
          return bcl.getLog();
        }
      }
    } catch (final Exception e) {
      log.error("Exception: reading batch state {}", e.getMessage());
    }
    return Collections.emptyList();
  }

  public int getLivySessionId() {
    return livySessionId;
  }

  public void setLivySessionId(final int livySessionId) {
    this.livySessionId = livySessionId;
  }

  public String getStatementResults(final int livySessionId, final int statementId, final int eventId) {
    String jsonStr = null;
    ResponseEntity<String> postForEntity;
    String state = LivyStatementState.waiting.toString();
    final String available = LivyStatementState.available.toString();
    final String url = livyBaseUrl + livySessionId + "/statements/" + statementId;
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = null;
      
      String output = "";
      String status = "";
      Date startTime = null;
      Date endTime = null;
      long elapsedTime = 0l;
      
      log.info("Current Statement Id is: {} and Event Id is: {}", statementId, eventId);
      
      Optional<EngFlowEvent> eventOp = engFlowEventService.getFlowEvent(eventId);
      EngFlowEvent event = null;
      if(eventOp.isPresent()) {
        event = eventOp.get();
      }
      
      while (!state.equals(available)) {
        postForEntity = restTemplate.getForEntity(url, String.class);
        
        final HttpStatus statusCode = postForEntity.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
          jsonStr = postForEntity.getBody();
          node = mapper.readValue(jsonStr, JsonNode.class);
          state = node.get("state").asText();
          
          if(event != null) {
            try {
              output = node.get("output").asText();
              status = node.get("output").get("status").asText();
              
              long lStartTime = node.get("started").asLong();
              startTime = new Date(lStartTime);

              long lEndTime = node.get("completed").asLong();
              endTime = new Date(lEndTime);
              
              elapsedTime = endTime.getTime() - startTime.getTime();
              event.setLivyStmtExecutionStatus(status);
              event.setLivyStmtOutput(output);
              event.setLivyStmtStartTime(startTime);
              event.setLivyStmtEndTime(endTime);
              event.setLivyStmtDuration(elapsedTime);
              
              engFlowEventService.save(event);
              
            }catch(Exception e) {
              // Do nothing.
            }
          }
        }
      }
      
    } catch (final Exception e) {
      log.error("Statement Execution Failed {}", e.getMessage());
    }
    log.info("Statement Execution Completed");
    return jsonStr;
  }

  public String livyPostHandler(final String url, final String payload) {
    String jsonStr = null;
    HttpStatus statusCode;
    ResponseEntity<String> postForEntity;
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    final HttpEntity<String> entity = new HttpEntity<>(payload, headers);
    log.info("PostHandler Payload : {}", payload);
    try {
      postForEntity = restTemplate.postForEntity(url, entity, String.class);
      statusCode = postForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        jsonStr = postForEntity.getBody();
      }
    } catch (final Exception e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
    return jsonStr;
  }
  
  public int runStatement(final int livySessionId, final String payload, final int eventId, final double progress) throws SQLException{
    final String url = livyBaseUrl + livySessionId + "/statements";
    final String sessionId = livyPostHandler(url, payload);
    log.info(" Session ID {}", sessionId);
    final JsonElement fromJson = gson.fromJson(sessionId, JsonElement.class);
    final int statementId = fromJson.getAsJsonObject().get("id").getAsInt();
    log.info(" Statement ID {}", statementId);
    
    Optional<EngFlowEvent> flowEventOp = engFlowEventService.getFlowEvent(eventId);
    EngFlowEvent eventObj = null;
    if(flowEventOp.isPresent()) {
      eventObj = flowEventOp.get();
      eventObj.setEventProgress(progress);
      engFlowEventService.save(eventObj);
    }
    
    return statementId;
  }
  
  public int runStatement(final int livySessionId, final String payload) throws SQLException{
    final String url = livyBaseUrl + livySessionId + "/statements";
    final String sessionId = livyPostHandler(url, payload);
    log.info(" Session ID {}", sessionId);
    final JsonElement fromJson = gson.fromJson(sessionId, JsonElement.class);
    final int statementId = fromJson.getAsJsonObject().get("id").getAsInt();
    log.info(" Statement ID {}", statementId);
    
    return statementId;
  }
  
  private static class SimpleTimerTask extends TimerTask{
    
    private boolean elapsed;
    private long duration;
    
    public SimpleTimerTask() {
      this.duration = 0l;
      this.elapsed = false;
    }
    
    public SimpleTimerTask(long duration) {
      this.duration = duration;
      this.elapsed = false;
    }
    
    public boolean isElapsed() {
      return this.elapsed;
    }
    
    @Override
    public void run() {
      int count = 0;
      while(count <= duration) {
        try {
          Thread.sleep(1);
          ++count;
        }catch(InterruptedException ie) {
          ie.printStackTrace();
        }
      }
      // After the elapsed time is over, set the elapsed flag to true.
      
      this.elapsed = true;
    }
    
    @Override
    public String toString() {
      return "{"+this.elapsed+"; "+this.duration+"}";
    }
  }
  
}
