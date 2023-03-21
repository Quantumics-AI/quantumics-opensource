package com.qs.api.repo;

import java.sql.SQLException;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qs.api.model.QsUdf;

@Repository
public interface UdfRepository extends JpaRepository<QsUdf, Integer> {
	
	List<QsUdf> findAllByProjectId(int projectId) throws SQLException;
	
	QsUdf getByUdfIdAndProjectIdAndUserIdAndActive(int udfId, int projectId, int userId, boolean active) throws SQLException;
	
}
