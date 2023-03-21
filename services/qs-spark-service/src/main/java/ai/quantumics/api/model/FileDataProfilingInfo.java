package ai.quantumics.api.model;

import java.util.Date;
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
@Table(name = "qsp_file_data_profiling_info")
public class FileDataProfilingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int dataProfileId;

    private int projectId;
    private int folderId;
    private int fileId;
    private String fileProfilingInfo;
    private String statsType;
    private boolean active;
    private Date creationDate;
    private Date modifiedDate;
}
