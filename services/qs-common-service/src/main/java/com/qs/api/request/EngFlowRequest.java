package com.qs.api.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EngFlowRequest {
    private int projectId;
    private int userId;
    private int engFlowId;
    private int eventId;
    private int autoConfigEventId;

    private String fileType;
}
