package com.qs.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "QS_Project")
@Data
public class Projects {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int projectId;

    private int userId;
    private String rawDb;
    private String orgName;
    private Date createdDate;
    private String bucketName;
    private String rawCrawler;
    private String projectName;
    private String projectDesc;
    private String processedDb;
    private String dbSchemaName;
    private String projectOutcome;
    private String projectDataset;
    private String projectEngineer;
    private String projectAutomate;
    private String processedCrawler;
}
