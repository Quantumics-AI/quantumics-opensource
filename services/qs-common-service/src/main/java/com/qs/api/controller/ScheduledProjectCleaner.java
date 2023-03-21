package com.qs.api.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RestController
public class ScheduledProjectCleaner {
  private final ControllerHelper helper;
  public ScheduledProjectCleaner(ControllerHelper helper) {
    this.helper = helper;
  }
  
  // Project cleanup operation is scheduled to run every three days.
  @Scheduled(fixedDelay = 259200000, initialDelay = 3600000)
  public void fixedDelaySch() {
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
     Date now = new Date();
     String strDate = sdf.format(now);
     log.info("Fixed Delay scheduler:: {}", strDate);
     
     helper.invokeProjectCleanupOp();
  }

}
