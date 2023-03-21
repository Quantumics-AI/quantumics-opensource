package com.qs.api.util;

import static java.lang.Thread.sleep;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class QsCustomLivyClient {

  /*private final RestTemplate restTemplate = new RestTemplate();
  private int livySessionId = -1;
  
  private final Gson gson = new Gson();

  @Value("${qs.livy.base.url}")
  private String livyBaseUrl;

  public int getLivySessionId() {
    return livySessionId;
  }

  public void setLivySessionId(final int livySessionId) {
    this.livySessionId = livySessionId;
  }

  public int getApacheLivySessionId(final String sessionName) {
    int retVal = 0;
    final String url = livyBaseUrl;
    HttpStatus statusCode;
    JsonElement elementTemp;
    final JsonObject payload = new JsonObject();
    payload.addProperty("kind", "pyspark");
    payload.addProperty("name", sessionName);
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
  
  public String getApacheLivySessionState(final int sessionId) {
    final String url = livyBaseUrl + sessionId + "/state";
    String jsonStr = null;
    JsonElement fromJson;
    HttpStatus statusCode;
    ResponseEntity<String> getForEntity;
    String state = LivySessionState.not_started.toString();
    try {
      getForEntity = restTemplate.getForEntity(url, String.class);
      statusCode = getForEntity.getStatusCode();
      if (statusCode.is2xxSuccessful()) {
        jsonStr = getForEntity.getBody();
        fromJson = gson.fromJson(jsonStr, JsonElement.class);
        state = fromJson.getAsJsonObject().get("state").getAsString();
      }
    } catch (final Exception e) {
      log.error("Exception: reading session state {}", e.getMessage());
    }
    return state;
  }
  
  public String getIdleApacheLivySessionState(final int sessionId) {
    final String url = livyBaseUrl + sessionId + "/state";
    String jsonStr = null;
    JsonElement fromJson;
    HttpStatus statusCode;
    ResponseEntity<String> postForEntity;
    final String idle = LivySessionState.idle.toString();
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
    return state;
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
        log.info("livyPostHandler Response : {}", jsonStr);
      }
    } catch (final Exception e) {
      log.error("Exception: establishing a session {}", e.getMessage());
    }
    return jsonStr;
  }*/
}
