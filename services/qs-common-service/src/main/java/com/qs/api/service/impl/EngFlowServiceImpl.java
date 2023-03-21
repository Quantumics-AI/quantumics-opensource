/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai. Morbi non lorem porttitor neque
 * feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan. Etiam sed turpis ac ipsum condimentum
 * fringilla. Maecenas magna. Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque
 * sagittis ligula eget metus. Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.qs.api.service.impl;

import com.qs.api.model.EngFlow;
import com.qs.api.repo.EngFlowRepository;
import com.qs.api.service.EngFlowService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class EngFlowServiceImpl implements EngFlowService {
  private final EngFlowRepository engFlowRepo;

  public EngFlowServiceImpl(EngFlowRepository engFlowRepo) {
    this.engFlowRepo = engFlowRepo;
  }

  @Override
  public EngFlow save(EngFlow engFlow) throws SQLException {
    return engFlowRepo.save(engFlow);
  }

  @Override
  public EngFlow saveIfNotPresent(EngFlow engFlow) throws SQLException {
    return null;
  }

  @Override
  public List<EngFlow> getFlowsForProjectAndFlowName(int projectId, String flowName)
      throws SQLException {
    return engFlowRepo.findByProjectIdAndEngFlowName(projectId, flowName);
  }

  @Override
  public Optional<EngFlow> getFlowsForProjectAndId(int projectId, int engFlowId)
      throws SQLException {
    return engFlowRepo.findByProjectIdAndEngFlowId(projectId, engFlowId);
  }

  @Override
  public List<EngFlow> getFlowForUser(int userId, String flowName) throws SQLException {
    return engFlowRepo.findByUserIdAndEngFlowName(userId, flowName);
  }

  @Override
  public List<EngFlow> getFlowForUserFromFlow(int userId, int engFlowId) throws SQLException {
    return engFlowRepo.findByUserIdAndEngFlowId(userId, engFlowId);
  }

  @Override
  public List<EngFlow> getFlowNames(int projectId, int userId) throws SQLException {
    return engFlowRepo.findByProjectIdAndUserId(projectId, userId);
  }

  @Override
  public void deleteEngFlow(int engFlowId) throws SQLException {
    engFlowRepo.deleteById(engFlowId);
  }

  @Override
  public void deleteEngFlow(String engFlowName) throws SQLException {
    engFlowRepo.deleteByEngFlowName(engFlowName);
  }

  @Override
  public Optional<EngFlow> getEngFlow(int engFlowId) throws SQLException {
    return engFlowRepo.findByEngFlowId(engFlowId);
  }

  @Override
  public List<EngFlow> getEngFlows(int parentEngFlowId) throws SQLException {
    return engFlowRepo.findByParentEngFlowId(parentEngFlowId);
  }

  @Override
  public List<EngFlow> getChildEngFlows(int parentEngFlowId) throws SQLException {
    return engFlowRepo.findByParentEngFlowIdOrderByCreatedDateDesc(parentEngFlowId);
  }
}
