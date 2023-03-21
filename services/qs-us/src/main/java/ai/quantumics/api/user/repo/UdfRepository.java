package ai.quantumics.api.user.repo;

import java.sql.SQLException;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.quantumics.api.user.model.QsUdf;

@Repository
public interface UdfRepository extends JpaRepository<QsUdf, Integer> {

    List<QsUdf> findAllByProjectIdAndUserIdAndActive(int projectId, int userId, boolean active) throws SQLException;
	
	QsUdf findByUdfIdAndProjectIdAndUserIdAndActive(int udfId, int projectId, int userId, boolean active) throws SQLException;
}
