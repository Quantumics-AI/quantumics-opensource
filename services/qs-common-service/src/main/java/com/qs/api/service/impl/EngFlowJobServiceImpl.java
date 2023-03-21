/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.qs.api.service.impl;

import com.qs.api.model.EngFlowJob;
import com.qs.api.repo.EngFlowJobRepository;
import com.qs.api.service.EngFlowJobService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EngFlowJobServiceImpl implements EngFlowJobService {

    private final EngFlowJobRepository flowJobRepository;

    public EngFlowJobServiceImpl(EngFlowJobRepository flowJobRepository) {
        this.flowJobRepository = flowJobRepository;
    }

    @Override
    public EngFlowJob save(EngFlowJob engFlowJob) {
        return flowJobRepository.save(engFlowJob);
    }

    @Override
    public List<EngFlowJob> getJobs(int engFlowId) {
        return flowJobRepository.findByEngFlowId(engFlowId);
    }

    @Override
    public Optional<EngFlowJob> getThisJob(int engFlowJobId) {
        return flowJobRepository.findById(engFlowJobId);
    }
}
