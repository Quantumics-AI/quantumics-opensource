package com.qs.api.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "QSP_File")
public class QsFiles extends MetaData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int fileId;

    private int userId;
    private int projectId;
    private int folderId;
    private String fileName;
    private int fileVersionNum;
    private String fileSize;
    private String fileType;

    @Column(columnDefinition = "TEXT")
    private String qsMetaData;

    private String ruleCreatedFrom;
    private Date createdDate;
    private Date ModifiedDate;
}
