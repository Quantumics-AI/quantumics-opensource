package ai.quantumics.api.user.repo;

import ai.quantumics.api.user.model.QsUserConsent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserConsentRepository extends PagingAndSortingRepository<QsUserConsent, Integer> {
        Page<QsUserConsent> findAllByUserId(int userId, Pageable pageable);
}
