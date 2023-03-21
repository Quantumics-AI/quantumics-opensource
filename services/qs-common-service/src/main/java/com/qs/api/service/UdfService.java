package com.qs.api.service;

import java.sql.SQLException;
import java.util.List;

import com.qs.api.model.QsUdf;

public interface UdfService {
	
	List<QsUdf> getUdfByProjectId(int projectId) throws SQLException;
	
	QsUdf getByUdfIdAndProjectIdAndUserId(int udfId, int projectId, int userId) throws SQLException;
	
	QsUdf save(QsUdf qsUdf) throws SQLException;
}
