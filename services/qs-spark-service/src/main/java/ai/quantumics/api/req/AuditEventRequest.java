package ai.quantumics.api.req;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuditEventRequest {

    private String startDate;

    private String endDate;
}
