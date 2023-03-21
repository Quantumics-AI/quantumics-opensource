package com.qs.api.repo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.qs.api.model.DataSourceInfo;

@Repository
public interface DataSourceInfoRepository extends CrudRepository<DataSourceInfo, Integer>{
  
  List<DataSourceInfo> findByProjectIdAndType(int projectId, String type);
  
}
