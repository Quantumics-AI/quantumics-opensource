package ai.quantumics.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.quantumics.api.model.QsaiCustomerInfo;


public interface QsaiCustomerInfoRepository extends JpaRepository<QsaiCustomerInfo, Integer> {

}
