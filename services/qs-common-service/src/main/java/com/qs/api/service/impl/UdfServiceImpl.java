package com.qs.api.service.impl;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qs.api.model.QsUdf;
import com.qs.api.repo.UdfRepository;
import com.qs.api.service.UdfService;

@Service
public class UdfServiceImpl implements UdfService {

	private final UdfRepository udfRepository;
	
	public UdfServiceImpl(UdfRepository udfRepository) {
		this.udfRepository = udfRepository;
	}
	
	@Override
	public List<QsUdf> getUdfByProjectId(int projectId) throws SQLException {
		return udfRepository.findAllByProjectId(projectId);
	}

	@Override
	public QsUdf getByUdfIdAndProjectIdAndUserId(int udfId, int projectId, int userId) throws SQLException {
		return udfRepository.getByUdfIdAndProjectIdAndUserIdAndActive(udfId, projectId, userId, true);
	}
	
	@Override
	public QsUdf save(QsUdf qsUdf) throws SQLException {
		return udfRepository.save(qsUdf);
	}

}
