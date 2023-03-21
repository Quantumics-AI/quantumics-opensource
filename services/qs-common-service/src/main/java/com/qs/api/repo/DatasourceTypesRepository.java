package com.qs.api.repo;

import java.sql.SQLException;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qs.api.model.QsDatasourceTypes;

@Repository
public interface DatasourceTypesRepository extends JpaRepository<QsDatasourceTypes, Integer>{
	
	List<QsDatasourceTypes> findAllByActive(boolean active) throws SQLException;
	
	List<QsDatasourceTypes> findByDataSourceTypeAndActive(String datasourceType, boolean active) throws SQLException;
}
