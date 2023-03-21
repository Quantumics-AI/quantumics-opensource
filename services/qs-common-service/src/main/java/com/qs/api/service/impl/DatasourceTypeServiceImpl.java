package com.qs.api.service.impl;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qs.api.model.QsDatasourceTypes;
import com.qs.api.repo.DatasourceTypesRepository;
import com.qs.api.service.DatasourceTypeService;

@Service
public class DatasourceTypeServiceImpl implements DatasourceTypeService {

	private final DatasourceTypesRepository dsTypesRepo;
	
	public DatasourceTypeServiceImpl(DatasourceTypesRepository dsTypesRepo) {
		this.dsTypesRepo = dsTypesRepo;
	}
	
	@Override
	public List<QsDatasourceTypes> getAllActiveDsTypes() throws SQLException{
		return dsTypesRepo.findAllByActive(true);
	}

	@Override
	public List<QsDatasourceTypes> getDataSourceTypes(String dataSourceType) throws SQLException{
		return dsTypesRepo.findByDataSourceTypeAndActive(dataSourceType, true);
	}
}
