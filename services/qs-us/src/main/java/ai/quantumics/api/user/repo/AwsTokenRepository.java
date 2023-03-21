package ai.quantumics.api.user.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.quantumics.api.user.model.QsAwsToken;

public interface AwsTokenRepository extends JpaRepository<QsAwsToken, Integer> {

	Optional<QsAwsToken> findByUuidAndUsedFalse(String uuid);

}
