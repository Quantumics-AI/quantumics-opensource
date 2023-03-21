/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.qs.api.service.impl;

import com.qs.api.model.EngFlowEvent;
import com.qs.api.repo.EngFlowEventRepository;
import com.qs.api.service.EngFlowEventService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class EngFlowEventServiceImpl implements EngFlowEventService {
    private final EngFlowEventRepository engFlowEventRepo;

    public EngFlowEventServiceImpl(EngFlowEventRepository engFlowEventRepo) {
        this.engFlowEventRepo = engFlowEventRepo;
    }

    @Override
    public EngFlowEvent save(EngFlowEvent engFlowEvent) throws SQLException {
        return engFlowEventRepo.save(engFlowEvent);
    }

    @Override
    public List<EngFlowEvent> getAllEventsData(int engFlowId) throws SQLException {
        return engFlowEventRepo.findByEngFlowId(engFlowId);
    }

    @Override
    public Optional<EngFlowEvent> getFlowEvent(int engFlowEventId) throws SQLException {
        return engFlowEventRepo.findById(engFlowEventId);
    }

    @Override
    public void deleteByEventId(int engFlowEventId) throws SQLException {
        engFlowEventRepo.deleteById(engFlowEventId);
    }

    @Override
    public List<EngFlowEvent> getFileEvents(int engFlowId, String eventType) throws SQLException {
        return engFlowEventRepo.findByEngFlowIdAndEventType(engFlowId, eventType);
    }
    
    @Override
    public List<EngFlowEvent> getEngFlowEventByTypes(int engFlowId, int projectId,
        Collection<String> eventTypes) throws SQLException {
      return engFlowEventRepo.findByEngFlowIdAndProjectIdAndEventTypeIn(engFlowId, projectId, eventTypes);
    }
}
