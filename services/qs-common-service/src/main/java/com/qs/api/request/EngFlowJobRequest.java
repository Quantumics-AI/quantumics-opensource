package com.qs.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngFlowJobRequest {
    private int engFlowId;
    private int flowJobId;
    private String engFlowName;
    private String status;
    private Date runDate;
    private String batchJobLog;
    private String autorunReqPayload;
}
