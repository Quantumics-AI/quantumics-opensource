package ai.quantumics.api.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.AppIncidents;
import ai.quantumics.api.model.AppSystemStatus;
import ai.quantumics.api.repo.AppIncidentsRepository;
import ai.quantumics.api.repo.AppSystemStatusRepository;
import ai.quantumics.api.service.AppStatusService;

@Service
public class AppStatusServiceImpl implements AppStatusService {
  
  private final AppIncidentsRepository incidentsRepo;
  private final AppSystemStatusRepository sysStatusRepo;
  
  public AppStatusServiceImpl(AppIncidentsRepository incidentsRepo, AppSystemStatusRepository sysStatusRepo) {
    this.incidentsRepo = incidentsRepo;
    this.sysStatusRepo = sysStatusRepo;
  }
  
  @Override
  public List<AppIncidents> getAppIncidentsInfo() {
    return incidentsRepo.findAll();
  }
  
  @Override
  public List<AppSystemStatus> getAppSystemStatusInfo() {
    return sysStatusRepo.findAll();
  }
  
}
