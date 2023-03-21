package com.qs.api.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.qs.api.model.DataSourceInfo;
import com.qs.api.repo.DataSourceInfoRepository;
import com.qs.api.service.DataSourceInfoService;

@Service
public class DataSourceInfoServiceImpl implements DataSourceInfoService {

  private final DataSourceInfoRepository dataSourceRepo;
  
  public DataSourceInfoServiceImpl(DataSourceInfoRepository dataSourceRepo) {
    this.dataSourceRepo = dataSourceRepo;
  }
  
  @Override
  public List<DataSourceInfo> getDataSourcesByType(int projectId, String type) {
    return dataSourceRepo.findByProjectIdAndType(projectId, type);
  }

  @Override
  public DataSourceInfo saveFileInfo(DataSourceInfo dataSource) {
    return dataSourceRepo.save(dataSource);
  }

}
