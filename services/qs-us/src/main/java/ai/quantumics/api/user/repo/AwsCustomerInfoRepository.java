package ai.quantumics.api.user.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.quantumics.api.user.model.QsAwsCustomerInfo;

public interface AwsCustomerInfoRepository extends JpaRepository<QsAwsCustomerInfo, Integer>{
	QsAwsCustomerInfo findByUserId(int userId);
	List<QsAwsCustomerInfo> findByCustomerIdentifierAndProductCode(String customerIdentifier,String productCode);
}
