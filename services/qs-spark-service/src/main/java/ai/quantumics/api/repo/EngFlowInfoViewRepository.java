package ai.quantumics.api.repo;

import ai.quantumics.api.model.EngFlowInfoView;
import ai.quantumics.api.vo.EngFlowInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EngFlowInfoViewRepository extends CrudRepository<EngFlowInfoView, Integer> {

    List<EngFlowInfoView> findByProjectIdAndUserIdAndActiveTrueOrderByCreatedDateDesc(int projectId, int userId);
}
