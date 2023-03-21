package ai.quantumics.api.user.service;

import java.util.List;
import java.util.Optional;

import ai.quantumics.api.user.model.QsAwsCustomerInfo;
import ai.quantumics.api.user.model.QsAwsToken;
import ai.quantumics.api.user.model.QsaiCustomerInfo;

public interface CustomerService {
	Optional<QsAwsToken> getToken(String uuid);

	QsAwsToken save(QsAwsToken awsToken);

	QsAwsCustomerInfo save(QsAwsCustomerInfo awsCustomerInfo);

	QsAwsCustomerInfo findByUserId(int userId);

	List<QsAwsCustomerInfo> findByCustomerIdentifierAndProductCode(String customerIdentifier, String productCode);

	QsaiCustomerInfo save(QsaiCustomerInfo qsaiCustomerInfo);
}
