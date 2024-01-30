package ai.quantumics.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "qsp_engflow_data_with_status")
public class EngFlowInfoView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int engFlowId;
    private int projectId;
    private int userId;
    private String engFlowName;
    private String engFlowDesc;
    private String engFlowMetaData;
    private String engFlowConfig;
    private boolean active;
    private Date createdDate;
    private String createdBy;
    private String engFlowDisplayName;
    private String jobStatus;
    private Date jobRunDate;
}
