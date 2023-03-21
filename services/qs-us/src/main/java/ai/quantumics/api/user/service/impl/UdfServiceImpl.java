package ai.quantumics.api.user.service.impl;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Service;


import ai.quantumics.api.user.model.QsUdf;
import ai.quantumics.api.user.repo.UdfRepository;
import ai.quantumics.api.user.service.UdfService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UdfServiceImpl implements UdfService {

private final UdfRepository udfRepository;
	
	public UdfServiceImpl(UdfRepository udfRepository) {
		this.udfRepository = udfRepository;
	}
	
	@Override
	public List<QsUdf> getUdfByProjectIdAndUserId(int projectId, int userId) throws SQLException {
		return udfRepository.findAllByProjectIdAndUserIdAndActive(projectId, userId, true);
	}

	@Override
	public QsUdf getByUdfIdAndProjectIdAndUserId(int udfId, int projectId, int userId) throws SQLException {
		return udfRepository.findByUdfIdAndProjectIdAndUserIdAndActive(udfId, projectId, userId, true);
	}
	
	@Override
	public QsUdf save(QsUdf qsUdf) throws SQLException {
		return udfRepository.save(qsUdf);
	}
	
}

