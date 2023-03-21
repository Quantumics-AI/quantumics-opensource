/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.qs.api.service;

import com.qs.api.model.EngFlow;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EngFlowService {

    EngFlow save(EngFlow engFlow) throws SQLException;

    EngFlow saveIfNotPresent(EngFlow engFlow) throws SQLException;

    List<EngFlow> getFlowsForProjectAndFlowName(int projectId, String flowName) throws SQLException;

    Optional<EngFlow> getFlowsForProjectAndId(int projectId, int engFlowId) throws SQLException;

    Optional<EngFlow> getEngFlow(int engFlowId) throws SQLException;

    List<EngFlow> getEngFlows(int parentEngFlowId) throws SQLException;
    
    List<EngFlow> getChildEngFlows(int parentEngFlowId) throws SQLException;

    List<EngFlow> getFlowForUser(int userId, String flowName) throws SQLException;

    List<EngFlow> getFlowForUserFromFlow(int userId, int engFlowId) throws SQLException;

    List<EngFlow> getFlowNames(int projectId, int userId) throws SQLException;

    void deleteEngFlow(int engFlowId) throws SQLException;

    void deleteEngFlow(String engFlowName) throws SQLException;
}
