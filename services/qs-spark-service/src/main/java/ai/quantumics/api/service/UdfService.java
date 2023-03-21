package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;

import ai.quantumics.api.model.QsUdf;

public interface UdfService {
	List<QsUdf> getUdfByProjectIdAndUserId(int projectId, int userId) throws SQLException;
	
	QsUdf getByUdfIdAndProjectIdAndUserId(int udfId, int projectId, int userId) throws SQLException;
	
	QsUdf save(QsUdf qsUdf) throws SQLException;
}
