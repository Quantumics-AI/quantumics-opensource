package com.qs.api.service;

import java.sql.SQLException;
import java.util.List;

import com.qs.api.model.QsDatasourceTypes;

public interface DatasourceTypeService {
	
	List<QsDatasourceTypes> getAllActiveDsTypes() throws SQLException;
	
	List<QsDatasourceTypes> getDataSourceTypes(String dataSourceType) throws SQLException;
}
