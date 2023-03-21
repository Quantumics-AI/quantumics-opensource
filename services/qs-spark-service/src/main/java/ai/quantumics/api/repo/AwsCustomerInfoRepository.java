package ai.quantumics.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.quantumics.api.model.QsAwsCustomerInfo;


public interface AwsCustomerInfoRepository extends JpaRepository<QsAwsCustomerInfo, Integer>{
	QsAwsCustomerInfo findByUserId(int userId);
}
