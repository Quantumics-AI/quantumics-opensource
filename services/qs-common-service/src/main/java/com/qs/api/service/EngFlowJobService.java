/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.qs.api.service;

import com.qs.api.model.EngFlowJob;

import java.util.List;
import java.util.Optional;

public interface EngFlowJobService {

    EngFlowJob save(EngFlowJob engFlowJob);

    List<EngFlowJob> getJobs(int engFlowId);

    Optional<EngFlowJob> getThisJob(int engFlowJobId);
}
