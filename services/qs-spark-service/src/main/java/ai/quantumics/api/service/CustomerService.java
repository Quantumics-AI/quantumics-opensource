package ai.quantumics.api.service;

import java.util.Optional;

import ai.quantumics.api.model.QsAwsCustomerInfo;
import ai.quantumics.api.model.QsAwsToken;
import ai.quantumics.api.model.QsaiCustomerInfo;



public interface CustomerService {
	Optional<QsAwsToken> getToken(String uuid);

	QsAwsToken save(QsAwsToken awsToken);

	QsAwsCustomerInfo save(QsAwsCustomerInfo awsCustomerInfo);
	
	QsAwsCustomerInfo findByUserId(int userId);

	QsaiCustomerInfo save(QsaiCustomerInfo qsaiCustomerInfo);
}
