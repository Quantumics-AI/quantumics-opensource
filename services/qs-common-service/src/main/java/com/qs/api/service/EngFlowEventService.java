/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.qs.api.service;

import com.qs.api.model.EngFlowEvent;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EngFlowEventService {

    EngFlowEvent save(EngFlowEvent engFlowEvent) throws SQLException;

    List<EngFlowEvent> getAllEventsData(int engFlowId) throws SQLException;

    Optional<EngFlowEvent> getFlowEvent(int engFlowEventId) throws SQLException;

    void deleteByEventId(int engFlowEventId) throws SQLException;

    List<EngFlowEvent> getFileEvents(int engFlowId, String eventType) throws SQLException;
    
    List<EngFlowEvent> getEngFlowEventByTypes(int engFlowId, int projectId,
        Collection<String> eventTypes) throws SQLException ;
}
