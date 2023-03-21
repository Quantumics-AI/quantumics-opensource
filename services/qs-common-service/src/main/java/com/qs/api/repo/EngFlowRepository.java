/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.qs.api.repo;

import com.qs.api.model.EngFlow;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EngFlowRepository extends CrudRepository<EngFlow, Integer> {

    List<EngFlow> findByProjectIdAndEngFlowName(int projectId, String flowName);

    Optional<EngFlow> findByProjectIdAndEngFlowId(int projectId, int engFlowId);

    List<EngFlow> findByUserIdAndEngFlowName(int userId, String flowName);

    List<EngFlow> findByUserIdAndEngFlowId(int userId, int engFlowId);

    List<EngFlow> findByProjectIdAndUserId(int projectId, int userId);

    List<EngFlow> findByParentEngFlowId(int parentEngFlowId);
    
    List<EngFlow> findByParentEngFlowIdOrderByCreatedDateDesc(int parentEngFlowId);

    Optional<EngFlow> findByEngFlowName(String flowName);

    Optional<EngFlow> findByEngFlowId(int engFlowId);

    List<EngFlow> findAllByActive(boolean active);

    void deleteByEngFlowName(String engFlowName);

}
