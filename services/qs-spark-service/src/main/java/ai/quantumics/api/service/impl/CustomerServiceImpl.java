package ai.quantumics.api.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.quantumics.api.model.QsAwsCustomerInfo;
import ai.quantumics.api.model.QsAwsToken;
import ai.quantumics.api.model.QsaiCustomerInfo;
import ai.quantumics.api.repo.AwsCustomerInfoRepository;
import ai.quantumics.api.repo.AwsTokenRepository;
import ai.quantumics.api.repo.QsaiCustomerInfoRepository;
import ai.quantumics.api.service.CustomerService;



@Service
public class CustomerServiceImpl implements CustomerService {
	@Autowired
	private AwsTokenRepository awsTokenRepository;
	@Autowired
	private QsaiCustomerInfoRepository qsaiCustomerInfoRepository;
	@Autowired
	private AwsCustomerInfoRepository awsCustomerInfoRepository;

	@Override
	public Optional<QsAwsToken> getToken(String uuid) {
		return awsTokenRepository.findByUuidAndUsedFalse(uuid);
	}

	@Override
	public QsAwsToken save(QsAwsToken awsToken) {
		return awsTokenRepository.save(awsToken);
	}

	@Override
	public QsAwsCustomerInfo save(QsAwsCustomerInfo awsCustomerInfo) {
		return awsCustomerInfoRepository.save(awsCustomerInfo);
	}

	@Override
	public QsaiCustomerInfo save(QsaiCustomerInfo qsaiCustomerInfo) {
		return qsaiCustomerInfoRepository.save(qsaiCustomerInfo);
	}


	@Override
	public QsAwsCustomerInfo findByUserId(int userId) {
		return awsCustomerInfoRepository.findByUserId(userId);
	}
}
