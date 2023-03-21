package ai.quantumics.api.service;

import java.util.List;
import ai.quantumics.api.model.AppIncidents;
import ai.quantumics.api.model.AppSystemStatus;

public interface AppStatusService {
  
  List<AppSystemStatus> getAppSystemStatusInfo();
  
  List<AppIncidents> getAppIncidentsInfo();
  
}
