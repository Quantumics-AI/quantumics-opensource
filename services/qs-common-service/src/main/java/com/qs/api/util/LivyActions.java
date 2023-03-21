package com.qs.api.util;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LivyActions {
  /*private final QsCustomLivyClient livyClient;
  

  public LivyActions(QsCustomLivyClient livyClient) {
    this.livyClient = livyClient;
  }
  
  public int getLivySessionId(int userId) {
    int livySessionId = 0;
    final String sessionName = "project0" + userId;
    log.info("Global SessionID {} ", livySessionId);
    livySessionId = livyClient.getApacheLivySessionId(sessionName);
    
    return livySessionId;
  }
  
  public String getLivySessionState(int sessionId) {
    String state = livyClient.getApacheLivySessionState(sessionId);
    log.info("Global SessionID {} and SessionState is: {}", sessionId, state);
    
    return state;
  }
  
  public String getLivyIdleSessionState(int sessionId) {
    String state = livyClient.getIdleApacheLivySessionState(sessionId);
    log.info("Global SessionID {} and SessionState is: {}", sessionId, state);
    
    return state;
  }*/
  
}
