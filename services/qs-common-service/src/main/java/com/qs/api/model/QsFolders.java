package com.qs.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "QSP_Folder")
@Data
public class QsFolders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int folderId;

    private int userId;
    private int projectId;
    private int fileId;
    private String folderName;
    private String folderDesc;
    private String dataOwner;
    private String dataPrepFreq;
    private String cleansingRuleSetName;
    private String createdBy;
    private String modifiedBy;
    private Date createdDate;
    private Date modifiedDate;
    private int parentId;
    private boolean isExternal;
}
