package com.qs.api.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EngFlowEventRequest {
    private int projectId;
    private int userId;
    private int engFlowId;
    private int eventId;
    
    private String eventType;
}
