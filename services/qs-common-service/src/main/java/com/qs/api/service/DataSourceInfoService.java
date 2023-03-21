package com.qs.api.service;

import java.util.List;
import com.qs.api.model.DataSourceInfo;

public interface DataSourceInfoService {
  
  List<DataSourceInfo> getDataSourcesByType(int projectId, String type);
  
  DataSourceInfo saveFileInfo(DataSourceInfo dataSource);
  
}
