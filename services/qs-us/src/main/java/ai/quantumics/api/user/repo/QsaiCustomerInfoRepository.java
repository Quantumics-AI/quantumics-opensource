package ai.quantumics.api.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.quantumics.api.user.model.QsaiCustomerInfo;

public interface QsaiCustomerInfoRepository extends JpaRepository<QsaiCustomerInfo, Integer> {

}
