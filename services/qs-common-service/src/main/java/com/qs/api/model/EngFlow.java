/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.qs.api.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "qsp_eng_flow")
public class EngFlow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private int engFlowId;

    private String engFlowName;
    private String engFlowDesc;
    private int projectId;
    private int userId;

    @Column(columnDefinition = "TEXT")
    private String engFlowMetaData;

    @Column(columnDefinition = "TEXT")
    private String engFlowConfig;

    private int parentEngFlowId;
    private boolean active;
    private Date createdDate;
    private String createdBy;
    private Date ModifiedDate;
}
